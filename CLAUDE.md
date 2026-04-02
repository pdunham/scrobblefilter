# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ScrobbleFilter is a legacy Java web application (Google App Engine) that integrates Last.fm and Twitter. Users connect both accounts, add artists to a filter list, and the app tweets a weekly summary of their top-listened artists (excluding filtered ones): "I've been listening to [Artist 1], [Artist 2], and [Artist 3]."

## Build & Run

This project was originally built and deployed using **Eclipse with the Google App Engine plugin** — there is no Maven, Gradle, or Ant build file. To develop locally:

1. Install the [Google Cloud SDK](https://cloud.google.com/sdk) with the App Engine Java component.
2. Use `dev_appserver.sh war/` (or the Eclipse GAE plugin) to run locally.
3. Deploy with `appcfg.sh update war/` (legacy) or `gcloud app deploy`.

To run the single test file:
```
# From Eclipse: right-click ScrobbleListFetcherTest.java → Run As → JUnit Test
# Or via command line if you set up a classpath manually (no build tool configured)
```

## Architecture

**Stack:** Spring MVC 3.0.5 · Objectify 4.0rc1 (App Engine Datastore) · Twitter4J 3.0.3 · Last.fm REST API · JSP views

**Request flow (filter page):**
```
Browser → HelloController → NetworkedScrobbleListFetcher → Last.fm API (7-day top artists)
                                                         ↓
                         JSP view ← ScrobbleListParser (JSON → ScrobbledArtist objects)
                                    + filtered artist list removed
```

**Cron job flow (weekly tweet):**
```
App Engine scheduler (Tuesdays 10:00 CST) → /hello/cron/sendalltweets
→ TweeterCronJob → CronUserFetcher (all users with cron=true)
→ ScrobbleTweeter.doTweet() → builds tweet string → Twitter4J → posts status
```

**Key source packages under `src/scrobblefilter/`:**
- `web/` — Spring MVC controllers (`HelloController`, `RegistrationController`, `TwitterSignInController`) and `TweeterCronJob`
- `net/` — External API calls (`NetworkedScrobbleListFetcher`, `ScrobbleListParser`, `ScrobbleTweeter`)
- `model/` — Objectify entities: `User`, `FilteredArtist`, `Preferences`, `ScrobbledArtist`
- `das/` — Datastore access (`UserFetcher`, `CronUserFetcher`)

**Web config:** `war/WEB-INF/scrobblefilter-servlet.xml` defines all Spring beans. `war/WEB-INF/web.xml` maps servlets and sets security constraints (cron endpoint restricted to admin role).

## Credentials & Configuration

- **Last.fm API key** is hardcoded in `NetworkedScrobbleListFetcher`
- **Twitter OAuth consumer key/secret** are in `war/WEB-INF/twitter4j.properties` (not committed)
- **Per-user Twitter access tokens** are stored in the App Engine Datastore on the `User` entity
- App Engine app ID: `scrobblefilter` (`war/WEB-INF/appengine-web.xml`)

## Constraints

- Tweet construction is hardcoded for exactly 3 artists.
- Last.fm period is hardcoded to 7 days (`7day` parameter in the API call).
- Objectify v4.0rc1 is very old; its registration pattern differs from modern Objectify — entities are registered in `ObjectifyService.register()` calls at startup.
