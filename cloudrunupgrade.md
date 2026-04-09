# Cloud Run Upgrade Checklist

## Blockers for Cloud Run Deployment

### Critical

1. **Objectify 4.0rc1 + `appengine-api-1.0-sdk`** — The entire data layer (`User`, `FilteredArtist`, all `ofy()` calls) depends on the GAE runtime datastore API, which doesn't exist outside of GAE. This is the biggest item. You'd need to migrate to either:
   - Google Cloud Datastore client library (`google-cloud-datastore`)
   - Cloud Firestore (`google-cloud-firestore`)
   - A traditional DB like Cloud SQL with JPA/Hibernate

2. **No Dockerfile** — Cloud Run needs a container. You'd need one with Tomcat 10+ (Java 17) and the WAR deployed into it.

3. **No WAR packaging in Maven** — `pom.xml` declares `<packaging>jar</packaging>` and has no `maven-war-plugin`. Maven never produces a deployable artifact.

### High Priority

4. **`cron.xml` is GAE-specific and silently ignored** — The weekly tweet job (`/hello/cron/sendalltweets`) will never fire. Replace with a **Cloud Scheduler** job making an authenticated HTTP request to the endpoint.

5. **Cron endpoint has no authentication outside GAE** — The `<security-constraint>` restricting `/hello/cron/*` to the `admin` role is enforced by GAE's infrastructure. In Cloud Run, the endpoint is publicly accessible. You'd need to verify a Cloud Scheduler OIDC token in the handler.

6. **Hardcoded credentials** — `scrobblefilter.properties` (Last.fm key) and `twitter4j.properties` (Twitter OAuth secrets) are plaintext files in the WAR. Move them to Cloud Secret Manager and inject via environment variables.

7. **`selenium-server.jar` in `war/WEB-INF/lib/`** — 16MB unused JAR inflating the image. Remove it.

### Medium Priority

8. **HTTP sessions stored in memory** — In Cloud Run with multiple instances, users will lose sessions on instance rotation. Add Spring Session backed by Cloud Memorystore (Redis), or switch to stateless auth.

9. **Port binding** — Cloud Run injects a `PORT` environment variable. The servlet container in the Dockerfile needs to bind to it.

10. **Last.fm API uses `http://`** (`NetworkedScrobbleListFetcher.java:14`) — Should be `https://`.

## What's Already in Good Shape

- Spring 6.1.14 + Jakarta Servlet 5.0 (`jakarta.*` imports) — fully compatible with modern servlet containers (Tomcat 10+)
- Spring MVC wiring (`web.xml`, `scrobblefilter-servlet.xml`) — standard, no GAE-specific config
- `AppConfig` properties loading — already decoupled from GAE, just needs secrets externalized

The data layer migration (item 1) is by far the largest piece of work.
