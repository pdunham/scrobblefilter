# Changelog

All notable changes to ScrobbleFilter on the `master` branch, summarized by
date. Format loosely follows [Keep a Changelog](https://keepachangelog.com/).
The project does not use versioned releases, so entries are grouped by date.

## 2026-06-05

- **Bluesky support, Phase 2 — credential encryption.** Add `CredentialCrypto`
  (`scrobblefilter.util`), AES-256-GCM authenticated encryption for per-user
  secrets stored in Datastore (the upcoming Bluesky OAuth refresh token + DPoP
  private key). Random 96-bit IV per encryption; wire-format is
  `base64(iv ‖ ciphertext ‖ tag)`. The 256-bit key comes from a `CRED_ENC_KEY`
  env var (base64), sourced from Secret Manager like `CRON_TOKEN`. Added
  `CredentialCryptoTest` (round-trip, randomized IV, tamper rejection, wrong-key
  rejection, key-length validation). Not yet wired into Spring — that waits
  until `CRED_ENC_KEY` is plumbed into the runtime/test environment.
- **Bluesky support, Phase 3 — User entity fields.** Add `blueskyDid`,
  `blueskyHandle`, `blueskyRefreshTokenEnc`, `blueskyDpopKeyEnc`, and the
  per-platform opt-in `blueskyCron` to the `User` entity, following the existing
  `fromEntity`/`toEntity` empty→null coercion. The encrypted credential blobs are
  stored unindexed (they can exceed Datastore's 1500-byte indexed-string limit
  and never need indexing); `blueskyCron` stays indexed for the upcoming cron
  query. Backward-compatible — existing entities default to null/false, no
  migration needed. Extended `UserTest` with empty/absent/present cases.
- **Bluesky support, Phase 4a — JOSE dependency.** Add `nimbus-jose-jwt` (a
  normal Maven dependency, not a vendored JAR) for the ES256 signing the AT
  Protocol OAuth DPoP proofs require. Smoke test confirms ES256 sign/verify works
  in this JDK.

## 2026-06-04

- Schedule the weekly tweet with **Cloud Scheduler**. A `sendalltweets` HTTP job
  in `us-central1` calls `GET /hello/cron/sendalltweets` every Tuesday 10:00
  `America/Chicago` with the `CRON_TOKEN` as an `X-Cron-Token` header, restoring
  the cadence lost when the Cloud Run migration made GAE's `cron.xml` a no-op.
  Retries are disabled (the endpoint is not idempotent). Documented the create /
  run / pause commands in the `README`.
- Delete the now-dead `war/WEB-INF/cron.xml` (silently ignored under Cloud Run;
  Cloud Scheduler is the source of truth for the cadence).
- Coerce empty `token` / `tokenSecret` to `null` when loading a `User`.
  `toEntity` persists missing OAuth tokens as `""`, so the `getToken() == null`
  guard in `ScrobbleTweeter.doTweet` never tripped for a token-less user and we
  would attempt a guaranteed-401 Twitter call instead of skipping them. Now
  matches the existing `twitterName` coercion; added `UserTest` covering the
  empty / absent / present cases.
- Log `lastfmName` (the entity key, always present) instead of `twitterName` in
  the cron tweet loop, so a legacy user whose Twitter screen name was never
  captured no longer logs as `sent tweet for null`.
- **Bluesky support, Phase 1 — posting abstraction (no behavior change).**
  Introduce a `SocialPoster` interface (`platform` / `isConnected` /
  `isEnabledFor` / `post`) with `SocialPostException`, and a `StatusComposer`
  that builds the post text once per run (so Last.fm is fetched once regardless
  of target count). `ScrobbleTweeter` → `TwitterPoster` implementing the
  interface; `TweeterController` / `TweeterCronJob` →
  `SocialPostController` / `SocialPostCronJob` (class renames only — URLs
  unchanged), now fanning out over the enabled posters. Latent fix: a per-user
  compose failure (e.g. <3 artists) no longer aborts the whole cron batch.
  Added `StatusComposerTest`. Full plan in `specs/blueskyplan.md`.

## 2026-06-02

- Add a `specs/` "constitution": `mission.md`, `tech-stack.md`, and `roadmap.md`
  (the roadmap includes planned Bluesky / multi-platform posting support).
- Add this `CHANGELOG.md`.
- Refresh `CLAUDE.md` to describe the current Cloud Run / Spring 6 / Jakarta /
  Cloud Datastore stack (it had still described the pre-migration GAE stack).
- Document the full Cloud Run deploy runbook in the `README` (Cloud Build →
  Artifact Registry → `gcloud run deploy`, verification, and secret setup).

## 2026-06-01

- Fix remove-artist failing for artist names containing an ampersand: the
  remove link now URL-encodes the entity id so an `&` no longer truncates the
  query string. Added an end-to-end regression test for the ampersand case.

## 2026-05-08

- Gate `/hello/admin/*` and `/hello/cron/*` on Secret Manager tokens
  (`MIGRATE_TOKEN` / `CRON_TOKEN`), replacing GAE's role-based security
  constraints that no longer apply under Cloud Run.
- Coerce null property values to `""` when building entities during migration.

## 2026-05-07

- Re-key the `User` entity on `lastfmName` so identity is platform-independent
  (groundwork for posting to more than one social network).
- Fix Cloud Run deployment and Twitter OAuth callback issues.
- Merge the `upgrade` branch into `master`.
- Ignore local IDE state and secret files.

## 2026-05-06

- Upgrade the Twitter integration and fix several related bugs.

## 2026-04-14

- Add Playwright end-to-end tests (auto-starting the Datastore emulator, the app
  in Docker, and a Last.fm mock server) and update build/test documentation.
- Fix Playwright test failures and Datastore emulator networking.

## 2026-04-08 – 2026-04-09 (Cloud Run migration)

The bulk of the modernization off Google App Engine:

- Migrate to **Spring 6 / Jakarta EE** (`javax.*` → `jakarta.*`).
- Replace **Objectify** with the **Google Cloud Datastore** client library.
- Add a **Dockerfile** and switch Maven to **WAR packaging** for Cloud Run;
  iterate on the build image (Tomcat 10.1, Maven/Temurin 17) and the
  `.dockerignore` to handle system-scoped JARs.
- Move hardcoded secrets into an untracked properties file.
- Add the **JSTL** dependency (API + impl) that Tomcat does not bundle.
- Fix the root redirect to use `index.jsp` instead of a servlet welcome-file.
- Use versionless Spring schema URLs to avoid network fetches inside containers.

## 2026-04-01 – 2026-04-07

- Update the README; add Claude Code project files.

## 2022-12-20

- Change the default page title.

## 2013 (App Engine era)

- Bring all libraries and APIs up to date so the app builds and runs.
- Add the weekly-tweet **cron job** and the datastore query backing it; add the
  ability to edit the per-user cron setting.
- Enable security for the cron endpoint.
- Reset the Last.fm window to **7 days**.
- Assorted UI/styling tweaks, grammar fixes in the tweet text, and make the
  `/hello/welcome` page the default landing page.

## 2012

- Initial commit of the application deployed on Google App Engine (Last.fm +
  Twitter integration, artist filter list, weekly summary tweet).
