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

---

## Now (operational gaps to close)

These finish the Cloud Run migration and harden what already exists.

- **Cloud Scheduler for the weekly tweet.** GAE's `cron.xml` is silently ignored
  on Cloud Run, so the weekly post does not currently fire on a schedule. Add a
  Cloud Scheduler job that makes an authenticated request to
  `/hello/cron/sendalltweets` using the existing `CRON_TOKEN`.
- **Move `twitter4j.properties` to Secret Manager.** OAuth consumer secrets are
  currently baked into the image. Mount them from Secret Manager (volume or env)
  so no credentials live in the container. (Aligns with mission principle 4.)
- **Last.fm over HTTPS.** Confirm the Last.fm base URL uses `https://`.

---

## Next: Bluesky support (multi-platform posting)

**Goal:** let a user post their weekly summary to **Bluesky** instead of, or in
addition to, Twitter/X — the headline feature of this roadmap.

Why it fits: identity is already keyed on Last.fm name, not the social platform
(mission principle 3), so adding a second posting target is an additive change,
not a re-architecture.

Proposed shape:

1. **Introduce a posting abstraction.** Extract a `SocialPoster` interface
   (`post(user, statusText)`) and refactor the current Twitter path
   (`ScrobbleTweeter` / Twitter4J) to be one implementation behind it. This is
   the key enabling refactor.
2. **Add a Bluesky implementation** over the **AT Protocol**: authenticate with
   the user's handle + an **app password** to create a session
   (`com.atproto.server.createSession`), then publish an `app.bsky.feed.post`
   record (`com.atproto.repo.createRecord`). No vendored SDK exists; this is a
   small HTTPS/JSON client.
3. **Extend the data model & connect flow.** Store per-user Bluesky credentials
   (handle + app password / refresh token) on the `User` entity alongside the
   Twitter tokens. Add a "connect Bluesky" path to registration and the
   dashboard. Per-platform opt-in (extend the single `cron` flag to per-target
   enablement).
4. **Fan out at post time.** `TweeterCronJob` / the cron handler iterates a
   user's *enabled* targets and posts to each via its `SocialPoster`.
5. **Mind the differences.** Bluesky's limit is ~300 graphemes (vs Twitter's
   280); rich-text "facets" are optional. The current 3-artist post is short
   enough that length is not a near-term concern, but the abstraction should not
   assume Twitter's constraints.

Open questions to resolve in the spec: credential storage/encryption for app
passwords; whether posting is "either/or" or "both"; naming now that "tweet" is
no longer the only verb (e.g. `ScrobbleTweeter` → `ScrobblePoster`).

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
