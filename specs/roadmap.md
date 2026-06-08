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

---

## Now (operational gaps to close)

These finish the Cloud Run migration and harden what already exists.

- **Add a ScrobbleFilter account password (authentication).** *Security gap —
  highest priority.* Registration/login is by Last.fm name alone: anyone who
  enters an existing user's Last.fm name gets that user's session and can toggle
  their weekly posting, add/remove filtered artists, and **force a post to their
  linked Twitter/Bluesky accounts**. (The attacker can't authenticate *as* them on
  Twitter/Bluesky, but they can drive ScrobbleFilter's controls — including
  triggering real posts to the victim's accounts.) Fix:
  - Add a per-user app password set at first registration; store only a salted
    hash (e.g. bcrypt/PBKDF2 via a vendored or JDK-available KDF) on the `User`
    entity — never the plaintext.
  - On return, require Last.fm name **+ password**; only establish the session
    (`request.getSession().setAttribute("user", …)`) on a verified match.
    Constant-time hash comparison.
  - Decide migration for existing password-less users (e.g. set-a-password
    prompt on next login) and a reset path (low priority for a small user base).
  - Consider gating state-changing endpoints (`updateCronSetting`,
    `updateBlueskyCronSetting`, `addartist`, `removeartist`, `post`, `tweet`) on
    the authenticated session — they already read the session user, so the win is
    making the session itself trustworthy.
  - Note: HTTP sessions are in-memory and fragile across Cloud Run instances
    (see tech-debt below); a real auth story may also want a shared session store
    or stateless tokens.
- **Move `twitter4j.properties` to Secret Manager.** OAuth consumer secrets are
  currently baked into the image. Mount them from Secret Manager (volume or env)
  so no credentials live in the container. (Aligns with mission principle 4.)
- **Last.fm over HTTPS.** Confirm the Last.fm base URL uses `https://`.

---

## Later (product flexibility)

Lift the constraints called out in [mission.md](mission.md) as limitations:

- **Configurable artist count.** Remove the hardcoded "exactly 3 artists" in
  `ScrobbleTweeter.constructTweet`; make it a per-user preference with a sane
  default, and handle the <N-artists case gracefully.
- **Configurable time window.** Make the Last.fm period (`7day`) a preference
  (e.g. 7-day / 1-month / overall).
- **Post preview / approval.** Optionally let a user review the generated post
  before it goes out.

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
