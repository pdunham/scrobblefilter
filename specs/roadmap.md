# Roadmap

> **Status:** Draft. A prioritized view of where ScrobbleFilter is headed.
> Items are grouped by horizon, not committed dates. Each should be promoted to
> a real spec/issue before work starts.

## Recently shipped

- Migrated off Google App Engine / Objectify to **Cloud Run + Cloud Datastore**.
- Re-keyed `User` on `lastfmName` so identity is **platform-independent** — the
  groundwork for posting to more than one social network.
- Gated `/hello/admin/*` and `/hello/cron/*` on Secret Manager tokens.
- Fixed remove-artist failing for names containing an ampersand (URL encoding).
- Documented the Cloud Run deploy runbook in the README.
- Added a **Cloud Scheduler** job for the weekly tweet (Tue 10:00
  America/Chicago, authenticated with `CRON_TOKEN`), restoring the schedule that
  GAE's `cron.xml` no longer provides under Cloud Run.
- Shipped **Bluesky support (multi-platform posting)** — the headline feature.
  Behind a `SocialPoster` abstraction (Twitter is now one implementation), users
  connect a Bluesky account via **AT Protocol OAuth** (PKCE + PAR + DPoP),
  opt in per platform, and the weekly summary fans out to every enabled target;
  manual per-platform posting too. Per-user Bluesky credentials are **encrypted at
  rest** (`CredentialCrypto`, key from `CRED_ENC_KEY`). See the README for the
  `CRED_ENC_KEY` / `BLUESKY_CLIENT_ID` setup.
- Shipped **authentication via Last.fm Web Auth** — login is now delegated to
  Last.fm's OAuth flow (`lastfm/signin` → Last.fm → `lastfm/callback`, identity
  confirmed server-side via `auth.getSession`), closing the session-takeover gap
  and two IDOR holes (`addartist`, `filter`). This **replaced** the short-lived
  ScrobbleFilter-password scheme (PBKDF2 + pepper): no passwords are stored or
  handled, so there's nothing to reset and the email-recovery follow-on is moot.
  Design in [lastfmauth.md](lastfmauth.md).

---

## Now (operational gaps to close)

These finish the Cloud Run migration and harden what already exists.

- ~~**Account recovery via email reset link.**~~ **Dropped** — recovery was a
  follow-on to the ScrobbleFilter-password scheme, which has since been replaced
  by Last.fm Web Auth (see Recently shipped). With no password to forget, there's
  nothing to recover; account access is recovered through Last.fm itself.
- **Move `twitter4j.properties` to Secret Manager.** OAuth consumer secrets are
  currently baked into the image. Mount them from Secret Manager (volume or env)
  so no credentials live in the container. (Aligns with mission principle 4.)
- **Last.fm over HTTPS.** Confirm the Last.fm base URL uses `https://`.

---

## Later (product flexibility)

Lift the constraints called out in [mission.md](mission.md) as limitations:

- **Configurable artist count.** Remove the hardcoded "exactly 3 artists" in
  `StatusComposer.constructStatus`; make it a per-user preference with a sane
  default, and handle the <N-artists case gracefully.
- **Configurable time window.** Make the Last.fm period (`7day`) a preference
  (e.g. 7-day / 1-month / overall).
- **Post preview / approval.** Optionally let a user review the generated post
  before it goes out.
- **Unlink a social account.** Let a user disconnect Twitter or Bluesky from the
  dashboard: clear the stored credentials (token/tokenSecret, or the encrypted
  Bluesky refresh token + DPoP key + DID/handle), reset that platform's opt-in,
  and revoke the token server-side where supported. Today there's no in-app way
  to remove a linked account.
- **Multiple accounts per platform.** Allow more than one Twitter and/or Bluesky
  account per user (e.g. a personal and a band account). This is a data-model
  change: the single-valued fields on `User` (twitterName/token/tokenSecret;
  blueskyHandle/did/refresh/dpop) move to a collection of linked accounts —
  likely a child entity keyed by platform + account — each with its own opt-in,
  and the cron/manual fan-out iterates every enabled account. The `SocialPoster`
  seam already abstracts per-platform posting; the work is in the model, the
  connect/unlink flows, and the dashboard listing.

---

## Tech debt / housekeeping (ongoing)

- Replace vendored legacy JARs where practical: **Jackson 1.9.5** → modern
  `jackson-databind`; evaluate replacing **Twitter4J 3.0.3** with a maintained
  client or a direct API client consistent with the new `SocialPoster` layer.
- Reduce scriptlet JSPs in favour of a templating layer that auto-escapes, to
  prevent classes of bug like the ampersand URL issue.
- Session storage: HTTP sessions are in-memory, which is fragile across Cloud
  Run instances — consider stateless auth or a shared session store if traffic
  grows.

---

## Guiding constraints for all roadmap work

Every item above is subject to the principles in [mission.md](mission.md): the
filter is sacred, public posting is opt-in, secrets stay in the secret store,
and changes to the post/filter pipeline ship with end-to-end tests.
