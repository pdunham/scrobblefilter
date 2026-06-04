# Plan: Add Bluesky support (multi-platform posting)

## Context

The roadmap's headline feature is letting a user post their weekly "I've been
listening to …" summary to **Bluesky** in addition to (or instead of)
Twitter/X. This is an additive change rather than a re-architecture because
identity is already keyed on `lastfmName`, not the social platform
(`User` entity in `src/scrobblefilter/model/User.java`). Today the only posting
path is `ScrobbleTweeter.doTweet(User)` (Twitter v2 + a hand-rolled OAuth 1.0a
helper), invoked from `TweeterController` (manual "tweet it") and
`TweeterCronJob` (weekly cron, now driven by Cloud Scheduler).

**Decisions (confirmed with user):**
1. **Per-platform opt-in ("both").** Independent enable toggles per platform; at
   post time fan out to every enabled + connected target.
2. **App-layer encryption** of per-user Bluesky credentials at rest (not the
   plaintext-field approach used today for Twitter tokens).
3. **AT Protocol OAuth** for connecting Bluesky (not stored app passwords).

> ⚠️ **Scope note.** AT Proto OAuth is the heavy path: it mandates PKCE **+ PAR**
> (pushed auth requests) **+ DPoP** (sender-constrained tokens with server-issued
> nonce challenges), plus handle→DID→PDS→authorization-server resolution and a
> publicly-hosted client-metadata document. This is the bulk of the work and the
> main risk. An **app-password interim** (`com.atproto.server.createSession` with
> handle + app password, encrypted via the same Phase 2 crypto) is a drop-in that
> reuses Phases 1–3 and 5–7 and defers only the Phase 4 OAuth client — a viable
> off-ramp if we want to ship sooner. Plan proceeds with OAuth as chosen.

## Architecture

Introduce a posting seam and fan out over it:

- **`SocialPoster`** interface (new, `src/scrobblefilter/net/`):
  `void post(User user, String statusText) throws SocialPostException;` plus
  `boolean isEnabledFor(User)` (connected + opt-in true) and `String platform()`.
- **`StatusComposer`** (new): holds the platform-agnostic text build —
  `extractFilteredList()` + a renamed `constructStatus()` lifted out of
  `ScrobbleTweeter` (`ScrobbleTweeter.java:90-108`). Compose **once** per run so
  Last.fm is hit once regardless of target count.
- **`TwitterPoster`** = today's `ScrobbleTweeter` posting half, implementing
  `SocialPoster` (keep the OAuth1 + v2 `postTweet` logic at `ScrobbleTweeter.java:53-84`).
- **`BlueskyPoster`** (new): ensures a valid DPoP-bound access token (refresh if
  needed) then `com.atproto.repo.createRecord` for an `app.bsky.feed.post`.
- Wire posters as Spring beans and inject `List<SocialPoster>` into
  `TweeterController` / `TweeterCronJob` (mirrors how `userFetcher` is autowired;
  beans declared in `war/WEB-INF/scrobblefilter-servlet.xml`). Replaces the
  current `new ScrobbleTweeter()` (`TweeterController.java:22`, `TweeterCronJob.java:30`).

## Phased implementation

**Phase 1 — Posting abstraction (no behavior change).** Add `SocialPoster`,
`SocialPostException`, `StatusComposer`; make `ScrobbleTweeter`/`TwitterPoster`
implement it; wire beans + inject `List<SocialPoster>`. Existing manual + cron
paths call `composer.compose(user)` then post to each enabled poster. Twitter
behavior unchanged; full suite stays green.

**Phase 2 — Credential encryption.** New `CredentialCrypto` util: AES-256-GCM,
key from env `CRED_ENC_KEY` (base64 32-byte), sourced from **Secret Manager** and
injected on Cloud Run exactly like `CRON_TOKEN`/`MIGRATE_TOKEN`
(`System.getenv`, see `AdminAuth`). Format: base64(iv ‖ ciphertext ‖ tag). Unit
test round-trip. Used to encrypt/decrypt the stored Bluesky refresh token + DPoP
private key.

**Phase 3 — User model fields.** Add to `User.java`, following the exact
`fromEntity`/`toEntity` coercion pattern (`User.java:27-59`; empty→null for
credential strings as in the recent token fix): `blueskyDid`, `blueskyHandle`
(non-secret), `blueskyRefreshTokenEnc`, `blueskyDpopKeyEnc` (encrypted blobs,
stored/loaded verbatim — decryption happens in the poster, not the model), and
boolean `blueskyCron` (per-platform opt-in). Extend `UserTest` for the new
fields. New entities default cleanly; `MigrationController` backfill is optional
(its `str()` helper pattern applies if needed).

**Phase 4 — AT Protocol OAuth client (the heavy part).** New
`src/scrobblefilter/net/bluesky/` package:
- **Client metadata**: serve `/client-metadata.json` (public client; `client_id`
  = that HTTPS URL; `redirect_uri` = `…/hello/bluesky/callback`;
  `scope=atproto transition:generic`; `dpop_bound_access_tokens=true`).
- **Resolution**: handle → DID (`com.atproto.identity.resolveHandle`) → DID doc
  (`plc.directory`) → PDS → authorization server
  (`/.well-known/oauth-protected-resource` + `oauth-authorization-server`).
  Base URLs overridable via env (e.g. `BLUESKY_*`) for tests, mirroring
  `LASTFM_BASE_URL` (`NetworkedScrobbleListFetcher.java:14-17`).
- **DPoP**: ES256 JWT signing with `DPoP-Nonce` retry-once. Vendor
  `nimbus-jose-jwt` as a JAR (repo already vendors JARs via `lib.dir` +
  `system` scope in `pom.xml`) to avoid hand-rolling JOSE crypto.
- **Flow**: PKCE + PAR → redirect → callback exchanges code (PKCE verifier +
  DPoP) for DPoP-bound access + refresh tokens; persist encrypted refresh token
  + DPoP key (Phase 2) on the `User`.
- **`BlueskySignInController`** (`/hello/bluesky/signin` + `/hello/bluesky/callback`)
  mirrors `TwitterSignInController` (incl. the `X-Forwarded-Proto` callback-URL
  fix at `TwitterSignInController.java:51-56`).
- Reuse `java.net.http.HttpClient` (as in `OAuth1Helper`) and Jackson
  `ObjectMapper` (as in `ScrobbleListParser`) for all HTTP/JSON.

**Phase 5 — `BlueskyPoster`.** `post(user, text)`: decrypt creds, ensure a fresh
access token (refresh + DPoP nonce handling), POST `createRecord`
(`{repo: did, collection: "app.bsky.feed.post", record: {$type, text, createdAt:
Instant.now()}}`). ~300-grapheme limit (current 3-artist text is well under).

**Phase 6 — Fan-out at post time.** Update `CronUserFetcher` query to
`PropertyFilter.or(eq("cron", true), eq("blueskyCron", true))`
(`CronUserFetcher.java:20-23`). In `TweeterCronJob`/`TweeterController`, compose
text once and loop enabled posters; keep the existing per-target log line shape
and catch-per-poster so one platform failing doesn't block the other.

**Phase 7 — UI.** `war/WEB-INF/jsp/helloworld.jsp`: add a "connect Bluesky"
section next to the Twitter one (link to `/hello/bluesky/signin`) and a Bluesky
opt-in toggle mirroring the cron toggle (new `updateBlueskySetting` endpoint in
`RegistrationController`, modeled on `updateCronSetting` at
`RegistrationController.java:62-74`). `filteredlist.jsp`: per-platform post
action + error display.

**Phase 8 — Config / docs / deploy.** Create `CRED_ENC_KEY` in Secret Manager and
bind it (`gcloud run deploy --update-secrets`); document client-metadata hosting.
Update `README`, `CHANGELOG.md`, and move the roadmap item to "Recently shipped".

## Critical files

- New: `net/SocialPoster.java`, `net/SocialPostException.java`,
  `net/StatusComposer.java`, `net/bluesky/*` (OAuth client + `BlueskyPoster`),
  `web/BlueskySignInController.java`, `util/CredentialCrypto.java`,
  `war/WEB-INF/jsp` Bluesky snippets, a `/client-metadata.json` source.
- Modified: `net/ScrobbleTweeter.java` (→ `TwitterPoster`, implements interface),
  `model/User.java` (+fields, +getters/setters), `das/impl/CronUserFetcher.java`
  (OR query), `web/TweeterController.java` + `web/TweeterCronJob.java` (inject +
  fan out), `web/RegistrationController.java` (+opt-in endpoint),
  `war/WEB-INF/scrobblefilter-servlet.xml` (poster beans),
  `war/WEB-INF/web.xml` (client-metadata mapping if servlet-served), `pom.xml`
  (vendored JOSE jar), `test/scrobblefilter/model/UserTest.java`.

## Reuse (don't reinvent)

- HTTP: `java.net.http.HttpClient` exactly as `OAuth1Helper` uses it.
- JSON: Jackson `ObjectMapper` as in `net/ScrobbleListParser.java` (build request
  bodies via `ObjectMapper` rather than string-concat to avoid the `escapeJson`
  fragility in `ScrobbleTweeter`).
- Config/secret patterns: classpath `*.properties` loading, env-var base-URL
  override, Secret-Manager→env injection (`AdminAuth` + the Cloud Scheduler
  runbook in `README`).
- Persistence: `User.fromEntity`/`toEntity` coercion; `MigrationController.str()`.
- Connect flow + opt-in toggle: `TwitterSignInController` and
  `RegistrationController.updateCronSetting`.

## Verification

- **Unit**: `CredentialCrypto` round-trip; `UserTest` new-field coercion;
  `BlueskyPoster.createRecord` and the OAuth token exchange/refresh + DPoP-nonce
  retry against a mock authorization-server + PDS.
- **E2E (Playwright)**: extend the existing mock infra (which already mocks
  Last.fm and is launched by `playwright/playwright.config.ts`) with a mock AT
  Proto **authorization server + PDS**; point the app at it via the `BLUESKY_*`
  env overrides. Cover: connect flow (signin → PAR → callback → encrypted creds
  stored), per-platform opt-in toggle, and fan-out posting to both targets. The
  mock relaxes DPoP/PAR signature checks but exercises the request/redirect shape.
- **Pre-push gate**: the Playwright suite runs automatically before every
  `git push` via the configured hook, so regressions block the push.
- **Manual smoke** against real `bsky.social` with a throwaway account before
  enabling for real users.

## Notes / risks

- AT Proto OAuth (DPoP + PAR + nonce retry + identity resolution) is the riskiest
  piece; Phase 4 may warrant its own PR/checkpoint. App-password interim is the
  off-ramp (see scope note).
- Jackson 1.9.5 is ancient but adequate for these small payloads; not upgrading
  here.
- Twitter tokens remain plaintext-on-entity; encrypting them with the new
  `CredentialCrypto` is a reasonable follow-up (out of scope here).
- Work spans multiple PRs; each phase is independently reviewable and Phase 1
  ships with zero behavior change.
