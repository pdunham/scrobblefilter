# Tech Stack

> **Status:** Draft. Describes the stack *as it actually is today* after the
> migration off Google App Engine / Objectify to Cloud Run + the Cloud Datastore
> client library. (Note: the top-level `CLAUDE.md` still describes the older
> pre-migration stack and is out of date on this point.)

## Runtime & platform

| Concern | Choice |
|---|---|
| Language | Java 17 |
| Web framework | Spring MVC 6.1.14 |
| Servlet API | Jakarta Servlet 6.0 (`jakarta.*`) |
| Servlet container | Apache Tomcat 10.1 |
| Views | JSP (scriptlet-based) + JSTL 3.0 |
| Build | Maven (`war` packaging, `maven-war-plugin` 3.4.0) |
| Hosting | Google Cloud Run (project `scrobblefilter`, region `us-central1`) |
| Container | Multi-stage Dockerfile: `maven:3.9-eclipse-temurin-17` → `tomcat:10.1-jdk17-temurin`, deployed as Tomcat `ROOT.war` |

## Data

| Concern | Choice |
|---|---|
| Store | Google Cloud Datastore via `google-cloud-datastore` 2.19.1 |
| Access layer | `das/` (`UserFetcher` + impls), `model/DatastoreProvider` |
| Entities | `User`, `FilteredArtist`, `Preferences`, `ScrobbledArtist` |
| Identity key | `User` is keyed on `lastfmName` (platform-independent — see [mission.md](mission.md) principle 3) |

The app was migrated off **Objectify 4.0rc1 / the GAE datastore API**; there is
no remaining dependency on the App Engine runtime.

## External integrations

| Integration | How |
|---|---|
| Last.fm | REST API (`user.gettopartists`, `period=7day`, JSON). Base URL is overridable via `LASTFM_BASE_URL` (used to point at a mock in tests). API key from config. |
| Twitter/X | Twitter4J 3.0.3 (vendored JAR) + a custom `OAuth1Helper` implementing OAuth 1.0a request-token / access-token / signed-request flow against `api.twitter.com`. Per-user access tokens stored on the `User` entity. |
| JSON parsing | Jackson 1.9.5 (vendored "jackson-all" JAR; legacy) |

## Configuration & secrets

- App config via `AppConfig` (decoupled from any GAE specifics).
- **Secrets** live in Google Secret Manager and are injected as env vars on the
  Cloud Run service: `MIGRATE_TOKEN` (← `migrate-token`) and
  `CRON_TOKEN` (← `cron-token`).
- `twitter4j.properties` (OAuth consumer key/secret) is currently **baked into
  the image** on the classpath (`WEB-INF/classes/`) rather than mounted from
  Secret Manager — a known gap, see [roadmap.md](roadmap.md).

## Privileged endpoints

- `/hello/admin/*` and `/hello/cron/*` are gated by `AdminAuth` checking the
  `X-Admin-Token` / cron token against the injected secret (GAE's role-based
  `<security-constraint>` no longer applies under Cloud Run).

## Build, test, deploy

| Task | Command |
|---|---|
| Build | `mvn package -DskipTests` |
| Unit tests | `mvn test` (JUnit 4.13.2) |
| E2E tests | Playwright (`playwright/`) — auto-starts the Datastore emulator, the app in Docker, and a Last.fm mock |
| Deploy | Cloud Build → Artifact Registry → `gcloud run deploy` (full runbook in the repo `README`) |

## Notable legacy / vendored pieces

- Twitter4J 3.0.3 and Jackson 1.9.5 are old and vendored as system-scope JARs
  in `war/WEB-INF/lib/` rather than pulled from Maven Central.
- Views are scriptlet JSPs — values rendered into URLs must be explicitly
  encoded (e.g. `URLEncoder.encode`) since there's no templating layer doing it.
