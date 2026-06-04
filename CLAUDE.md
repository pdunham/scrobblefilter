# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ScrobbleFilter is a Java web application that integrates Last.fm and Twitter/X. Users connect both accounts, add artists to a filter list, and the app posts a weekly summary of their top-listened artists (excluding filtered ones): "I've been listening to [Artist 1], [Artist 2], and [Artist 3]."

It was originally a Google App Engine app and has since been **migrated to Google Cloud Run** (Spring 6 / Jakarta EE / Cloud Datastore client library). See `specs/` for the project's mission, tech stack, and roadmap.

## Build & Run

The project builds with **Maven** (`war` packaging) and runs in **Tomcat 10.1** via a multi-stage Dockerfile.

- Build the WAR: `mvn package -DskipTests`
- Unit tests: `mvn test`
- End-to-end tests: Playwright in `playwright/` — `cd playwright && npm test`. Playwright auto-starts the Cloud Datastore emulator, the app in Docker, and a Last.fm mock server. (Requires Docker, Node, and the gcloud SDK with the `beta` + `cloud-datastore-emulator` components.)
- Run locally: `docker build -t scrobblefilter .` then run the container with `DATASTORE_EMULATOR_HOST` / `LASTFM_BASE_URL` / `GOOGLE_CLOUD_PROJECT` set (see `playwright/playwright.config.ts` for the exact invocation).
- Deploy: Cloud Build → Artifact Registry → `gcloud run deploy`. The full deploy runbook is in the `README`.

## Architecture

**Stack:** Java 17 · Spring MVC 6.1.14 · Jakarta Servlet 6.0 (Tomcat 10.1) · `google-cloud-datastore` 2.19.1 · Twitter4J 3.0.3 + custom OAuth 1.0a helper · Last.fm REST API · Jackson 1.9.5 · JSP views. (Full detail in `specs/tech-stack.md`.)

**Request flow (filter page):**
```
Browser → HelloController → NetworkedScrobbleListFetcher → Last.fm API (7-day top artists)
                                                         ↓
                         JSP view ← ScrobbleListParser (JSON → ScrobbledArtist objects)
                                    + filtered artist list removed
```

**Cron job flow (weekly tweet):**
```
Cloud Scheduler job `sendalltweets` (us-central1, Tue 10:00 America/Chicago)
→ GET /hello/cron/sendalltweets (gated by CRON_TOKEN via X-Cron-Token header)
→ TweeterCronJob → CronUserFetcher (all users with cron=true)
→ ScrobbleTweeter.doTweet() → builds tweet string → Twitter4J → posts status
```
Note: GAE's `cron.xml` does **not** fire under Cloud Run, so the schedule is a
Cloud Scheduler job that calls the cron endpoint (see the README deploy runbook).

**Key source packages under `src/scrobblefilter/`:**
- `web/` — Spring MVC controllers (`HelloController`, `RegistrationController`, `TwitterSignInController`, `TweeterController`, `MigrationController`), `TweeterCronJob`, and `AdminAuth` (token gating)
- `net/` — External API calls (`net/impl/NetworkedScrobbleListFetcher`, `ScrobbleListParser`, `ScrobbleTweeter`, `OAuth1Helper`)
- `model/` — Datastore-backed entities: `User`, `FilteredArtist`, `Preferences`, `ScrobbledArtist`, plus `DatastoreProvider`
- `das/` — Datastore access (`UserFetcher` + `das/impl/` implementations including `CronUserFetcher`)

**Web config:** `war/WEB-INF/scrobblefilter-servlet.xml` defines the Spring beans; `war/WEB-INF/web.xml` maps servlets. Privileged endpoints (`/hello/admin/*`, `/hello/cron/*`) are gated in-app by `AdminAuth` checking a token against an injected secret — GAE's role-based `<security-constraint>` no longer applies on Cloud Run.

## Credentials & Configuration

- **Last.fm API key** is read via `AppConfig` (`lastfm.api.key`); the Last.fm base URL is overridable with the `LASTFM_BASE_URL` env var (used to point at a mock in tests).
- **Twitter OAuth consumer key/secret** are in `twitter4j.properties`, baked onto the classpath (`WEB-INF/classes/`) at build time (not committed). Moving this to Secret Manager is a roadmap item.
- **Per-user Twitter access tokens** are stored in Cloud Datastore on the `User` entity (which is keyed on `lastfmName`, so identity is platform-independent).
- **Admin/cron secrets** (`MIGRATE_TOKEN`, `CRON_TOKEN`) live in Google Secret Manager and are injected as env vars on the Cloud Run service.
- Cloud project / region / service: `scrobblefilter` / `us-central1` / `scrobblefilter`.

## Constraints

- Tweet construction is hardcoded for exactly 3 artists (`ScrobbleTweeter.constructTweet`).
- Last.fm period is hardcoded to 7 days (`period=7day` in the API call).
- Views are scriptlet JSPs with no auto-escaping layer — values rendered into URLs must be explicitly encoded (e.g. `URLEncoder.encode`).
