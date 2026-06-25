# lastfmauth.md — Replace Password Auth with Last.fm Web Auth

## Context

Login was previously implemented as a username + ScrobbleFilter-specific password. The goal is to eliminate that credential entirely and instead prove ownership of a Last.fm account by redirecting users through Last.fm's own Web Auth flow. This removes the `PASSWORD_PEPPER` secret, the `PasswordHasher` class, and all password hashing/verification logic. Users click "Sign in with Last.fm", authorize on Last.fm's site, and land on the dashboard — no ScrobbleFilter password ever chosen or stored.

## Last.fm Web Auth flow

1. `GET /hello/lastfm/signin` → 302 to `https://www.last.fm/api/auth/?api_key=KEY&cb=https://…/hello/lastfm/callback`
2. User logs in on Last.fm → redirected back to `GET /hello/lastfm/callback?token=TOKEN`
3. App calls `auth.getSession` API with the token (server-side) → response confirms the Last.fm username
4. App looks up or creates `User` by that username, drops old session, sets new session → redirect to `/hello/world`

The `api_sig` for `auth.getSession` is `MD5(sorted key+value pairs concatenated + shared_secret)` — standard Last.fm signature scheme.

---

## Prerequisites (before coding)

- **Last.fm shared secret** (`lastfm.api.secret`): retrieve from [Last.fm developer portal](https://www.last.fm/api/account/). Add to `war/WEB-INF/scrobblefilter.properties` (gitignored, dev only).
- **Callback URL registration**: add `https://scrobblefilter-mzrsyx44yq-uc.a.run.app/hello/lastfm/callback` to the allowed callback URLs in the Last.fm developer portal for your API key.

---

## Implementation steps

### 1. New controller — `LastfmSignInController.java`

Create `src/scrobblefilter/web/LastfmSignInController.java`. Component scan already covers `scrobblefilter.web`; no bean wiring needed.

**Three endpoints:**

`GET welcome` — move the welcome-page handler here from `RegistrationController` (returns `newuser` view).

`GET lastfm/signin` — redirect to Last.fm auth page:
```
LASTFM_AUTH_URL + "?api_key=" + enc(apiKey) + "&cb=" + enc(callbackUrl)
```
`LASTFM_AUTH_URL` is a `static final` field read from env (default `https://www.last.fm/api/auth/`), mirroring the `LASTFM_BASE_URL` pattern in `NetworkedScrobbleListFetcher`. Callback URL is constructed from `request.getRequestURL()` the same way `TwitterSignInController` does it (strip last path segment, append `/callback`, honour `X-Forwarded-Proto`).

`GET lastfm/callback` — exchange token for a confirmed username:
1. Read `?token=` param; redirect to welcome if missing.
2. Build sig: `TreeMap` of `{api_key, method=auth.getSession, token}` → MD5(sorted key+value pairs + secret).
3. Call `LASTFM_BASE_URL + "method=auth.getSession&api_key=…&token=…&api_sig=…&format=json"` via `HttpClient` (same static pattern as `OAuth1Helper`).
4. Parse `session.name` from JSON using `ObjectMapper` (Jackson 1.9.5 already on classpath — same imports as `ScrobbleListParser`).
5. Look up or create `User` via `RegistrationController.findUser(lastfmName)`; invalidate old session, set `"user"` attribute, redirect to `/hello/world`.

**Secret reading:** `System.getenv("LASTFM_API_SECRET")` first, fallback to `AppConfig.get("lastfm.api.secret")`, throw `IllegalStateException` if both missing. Mirrors `CredentialCryptoProvider` lazy-load pattern.

**Do not** store the Last.fm session key on `User` — it's used only to confirm identity and discarded.

### 2. Trim `RegistrationController.java`

Remove:
- `@Autowired private PasswordHasher passwordHasher` field and its import
- `GET welcome` handler (moved to `LastfmSignInController`)
- `POST register` (`welcomeUser`) method and all imports that become unused

Keep: `logout`, `updateCronSetting`, `updateBlueskyCronSetting`, `addartist`, `removeartist`, `findUser` — all unchanged.

### 3. Replace `newuser.jsp`

Strip the `<form>`, username/password inputs, and error display. Replace body with:
```html
<p>ScrobbleFilter posts your weekly top artists to Twitter or Bluesky.</p>
<p><a href="/hello/lastfm/signin">Sign in with Last.fm</a></p>
```

### 4. Update `scrobblefilter-servlet.xml`

Remove the `passwordHasher` bean. No new beans needed.

### 5. Delete password infrastructure

- `src/scrobblefilter/util/PasswordHasher.java`
- `test/scrobblefilter/util/PasswordHasherTest.java`

### 6. Clean up `User.java`

Remove: `passwordHash` field, `getPasswordHash()`, `setPasswordHash()`, `hasPassword()`, and the `passwordHash` read/write lines in `fromEntity()`/`toEntity()`. Existing Datastore entities retain the orphaned property harmlessly (Datastore is schemaless).

### 7. Clean up `UserTest.java`

Remove the three password-hash coercion test methods (`emptyPasswordHashCoercesToNull`, `absentPasswordHashIsNull`, `presentPasswordHashIsPreserved`).

### 8. Update mock server — `lastfm-mock-server.js`

The current server is a single catch-all. Add routing for two new paths:

**`GET /auth/setup?username=foo`** — stores `{ username }` keyed by a new token (e.g. `tok-<counter>`), returns `{ token }`. Used by e2e test helpers to pre-register the desired username before driving the browser flow.

**`GET /auth/`** — the simulated Last.fm auth page. Reads the stored pending token, redirects to `cb + "?token=" + pendingToken`. (Each call to `/auth/setup` queues one token; `/auth/` consumes it.)

**`method=auth.getSession` on `/2.0/`** — looks up the token in the store, returns `{ session: { name: storedUsername, key: "mock-session-key", subscriber: 0 } }`.

**Default** — returns the top-artists fixture as before.

### 9. Update `playwright.config.ts`

In the docker `command` array:
- Remove `-e PASSWORD_PEPPER=test-password-pepper`
- Add `-e LASTFM_AUTH_URL=http://host.docker.internal:9090/auth/`
- Add `-e LASTFM_API_SECRET=test-lastfm-secret`

### 10. Rewrite Playwright tests

**Extract shared helper to `playwright/tests/helpers.ts`:**
```typescript
export async function oauthLogin(page, request, lastfm: string) {
  const res = await request.get(`http://localhost:9090/auth/setup?username=${lastfm}`);
  const { token } = await res.json();   // reserve the username on the mock
  await page.goto('/hello/lastfm/signin');
  await page.waitForURL(/\/hello\/world/);
}
```

**`auth.spec.ts`** — rewrite all tests using `oauthLogin`. Replace:
- "registers with a password" → "signs in via Last.fm and lands on dashboard"
- "correct password logs in" → "returning user can sign in again"
- "wrong password rejected" → remove (no longer applicable)
- "registration without password shows error" → remove
- IDOR test → keep, update to use `oauthLogin`

**All other specs** (`registration.spec.ts`, `artist-filter.spec.ts`, `filtered-list.spec.ts`, `bluesky-*.spec.ts`, `smoke.spec.ts`) — replace their inline `register`/`setupUser` helpers with an import of `oauthLogin` from `helpers.ts`. In `smoke.spec.ts`, change the welcome-page assertion from `input[name="password"]` visible to `a[href*="lastfm/signin"]` visible.

### 11. Update `war/WEB-INF/scrobblefilter.properties`

Add: `lastfm.api.secret=<your shared secret from Last.fm developer portal>`

### 12. Documentation

**README** — replace the "Account password" section (PASSWORD_PEPPER, password-pepper secret) with a "Last.fm OAuth" section documenting:
- `LASTFM_API_SECRET` env var / `lastfm-api-secret` Secret Manager secret
- `LASTFM_AUTH_URL` (test override only; leave unset in prod)
- Callback URL registration requirement

**CLAUDE.md** — update Credentials section: remove `PASSWORD_PEPPER`, add `LASTFM_API_SECRET`.

**`specs/roadmap.md`** — note password auth replaced by Last.fm OAuth under recently shipped.

### 13. Cloud Run deployment

```bash
# Create the secret (paste the real shared secret)
printf 'YOUR_LASTFM_SHARED_SECRET' | gcloud secrets create lastfm-api-secret --data-file=-

SA=$(gcloud run services describe scrobblefilter --region=us-central1 \
     --format='value(spec.template.spec.serviceAccountName)')
gcloud secrets add-iam-policy-binding lastfm-api-secret \
     --member="serviceAccount:$SA" --role=roles/secretmanager.secretAccessor

# Rebuild, tag, deploy (remove PASSWORD_PEPPER, bind new secret)
gcloud builds submit --tag us-central1-docker.pkg.dev/scrobblefilter/scrobblefilter/app:latest
gcloud run deploy scrobblefilter \
  --image us-central1-docker.pkg.dev/scrobblefilter/scrobblefilter/app:latest \
  --region us-central1 \
  --update-secrets LASTFM_API_SECRET=lastfm-api-secret:latest \
  --remove-env-vars PASSWORD_PEPPER \
  --quiet
```

The `password-pepper` secret can be deleted from Secret Manager after deploy; no existing hashes need verification anymore.

---

## Critical files

| File | Action |
|------|--------|
| `src/scrobblefilter/web/LastfmSignInController.java` | **Create** — new auth controller |
| `src/scrobblefilter/web/RegistrationController.java` | Remove `welcome`/`register`/`passwordHasher` |
| `src/scrobblefilter/util/PasswordHasher.java` | **Delete** |
| `src/scrobblefilter/model/User.java` | Remove `passwordHash` field + methods |
| `war/WEB-INF/jsp/newuser.jsp` | Replace form with "Sign in with Last.fm" link |
| `war/WEB-INF/scrobblefilter-servlet.xml` | Remove `passwordHasher` bean |
| `war/WEB-INF/scrobblefilter.properties` | Add `lastfm.api.secret` |
| `playwright/lastfm-mock-server.js` | Add `/auth/setup`, `/auth/`, `auth.getSession` routing |
| `playwright/playwright.config.ts` | Swap `PASSWORD_PEPPER` → `LASTFM_AUTH_URL` + `LASTFM_API_SECRET` |
| `playwright/tests/helpers.ts` | **Create** — shared `oauthLogin` helper |
| `playwright/tests/auth.spec.ts` | Rewrite for OAuth flow |
| All other `playwright/tests/*.spec.ts` | Update register helpers to use `oauthLogin` |
| `test/scrobblefilter/util/PasswordHasherTest.java` | **Delete** |
| `test/scrobblefilter/model/UserTest.java` | Remove 3 password-hash test methods |

---

## Verification

1. `mvn test` — all unit tests pass (56 → ~50 after removing password tests)
2. `cd playwright && npm test` — all 30 e2e tests pass with the new OAuth flow
3. Smoke check prod after deploy: `curl -s -o /dev/null -w '%{http_code}' https://…/hello/welcome` returns 200; page shows "Sign in with Last.fm" link, no password field
