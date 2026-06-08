# Plan: ScrobbleFilter account password (authentication)

## Context

Login is by Last.fm name alone. `RegistrationController.register` (POST
`/hello/register`) calls `findOrCreateUser` and then
`session.setAttribute("user", …)` — so entering *any* existing user's Last.fm
name establishes their session, after which an attacker can toggle their weekly
posting, edit their filters, and **force posts to their linked Twitter/Bluesky
accounts**. Worse, `addArtistToFilter` looks the target user up from a **request
parameter** (`prefs.getLastfmName()`), so it mutates an account without even
needing a session. This work adds a per-user ScrobbleFilter password and ties
identity to an authenticated session, closing both holes.

**Decisions (confirmed):**
- **No email / recovery this phase.** Forgotten passwords → manual admin reset;
  an email + reset flow is a later phase.
- **Existing password-less accounts claim on next login** — the first login for
  an account with no stored hash sets its password.
- **PBKDF2 + per-user salt + app-wide pepper** (pepper from Secret Manager).

## Approach

### Password hashing — new `util/PasswordHasher` (JDK only, no new dependency)
- `PBKDF2WithHmacSHA256`, 16-byte random salt, ~210k iterations, 256-bit output
  (`javax.crypto.SecretKeyFactory` + `PBEKeySpec`).
- App-wide **pepper** from `PASSWORD_PEPPER` env (Secret Manager, like
  `CRED_ENC_KEY`), mixed into the PBKDF2 input so a Datastore-only leak can't be
  brute-forced.
- Stored value on the User: `pbkdf2$<iterations>$<base64 salt>$<base64 hash>`
  (salt embedded; pepper never stored).
- `verify(password, stored)` recomputes with the stored salt/iterations + env
  pepper and compares with `MessageDigest.isEqual` (constant-time — reuse the
  `AdminAuth` idiom).
- Lazy pepper access mirroring `CredentialCryptoProvider`: a test ctor takes an
  explicit pepper; the default reads `PASSWORD_PEPPER` on first use, so a missing
  pepper surfaces at login rather than crashing boot.

### User model — `model/User.java`
- Add `private String passwordHash;` using the established `fromEntity`/`toEntity`
  empty→null coercion (store unindexed, like the encrypted Bluesky blobs);
  getter/setter + a `hasPassword()` convenience.

### Registration + login — `web/RegistrationController.java`, `war/WEB-INF/jsp/newuser.jsp`
- Welcome form gains a **password** field (one form serves both sign-up and
  login). Require it server-side.
- Rewrite `register` (POST) to handle all cases by Last.fm name:
  - **New name** → create user, set hash from the entered password, save, log in.
  - **Existing + no hash (legacy claim)** → set hash from the entered password,
    save, log in.
  - **Existing + hash** → `verify`; match → log in; mismatch → re-render welcome
    with a generic "Last.fm name and password don't match" error and **no session**.
  - On success, rotate the session id (`request.changeSessionId()`) before
    `setAttribute("user", …)` to avoid session fixation.
- `findOrCreateUser` folds into this password-aware flow.

### Tie identity to the session (close the IDOR)
- **`addArtistToFilter`**: use the **session** user, not
  `findUser(prefs.getLastfmName())`; redirect to welcome when no session.
- **`HelloController.filter` (GET)**: identify by the session user; ignore a
  client-supplied `lastfmName` for identity (read-only, but stops viewing others'
  lists by name).
- The other state-changing endpoints (`updateCronSetting`,
  `updateBlueskyCronSetting`, `removeartist`, `SocialPostController.post`/`tweet`,
  the connect controllers) already read the session user — they become
  trustworthy once the session itself requires a password.

### Wiring
- `PasswordHasher` bean in `war/WEB-INF/scrobblefilter-servlet.xml`; `@Autowired`
  into `RegistrationController`.

## Critical files
- New: `src/scrobblefilter/util/PasswordHasher.java`,
  `test/scrobblefilter/util/PasswordHasherTest.java`.
- Modified: `model/User.java` (+passwordHash), `web/RegistrationController.java`
  (password-aware register + addartist session fix), `web/HelloController.java`
  (filter session identity), `war/WEB-INF/jsp/newuser.jsp` (password field),
  `war/WEB-INF/scrobblefilter-servlet.xml` (bean),
  `test/scrobblefilter/model/UserTest.java`, plus the Playwright specs/config below.

## Tests
- **Unit** (`PasswordHasherTest`, mirroring `CredentialCryptoTest` with an explicit
  pepper): hash/verify round-trip; wrong password rejected; distinct salts (same
  password → different stored hashes, both verify); pepper matters (hash under
  pepper A fails verify under pepper B); malformed stored value rejected.
  `UserTest`: passwordHash empty→null / present-preserved.
- **Playwright e2e**:
  - Add `PASSWORD_PEPPER` to the app container env in `playwright.config.ts`.
  - Update the shared `register`/`setupUser` helpers in **every** spec that
    registers (registration, artist-filter, filtered-list, bluesky-connect,
    bluesky-cron, bluesky-manual-post) to fill the new password field.
  - New `auth.spec.ts`: new user registers w/ password → dashboard; returning user
    with the wrong password → error and still bounced from `/hello/world`; correct
    password → dashboard. `smoke.spec` asserts the password field renders.
  - `addartist` IDOR regression: POST `/hello/addartist` with a foreign
    `lastfmName` and no session → does not mutate that account (redirect to welcome).
  - The legacy-claim path is hard to exercise on the fresh emulator (no
    hash-less user) — cover it at the controller/`PasswordHasher` level + a manual
    prod check.

## Config / deploy (after merge)
- Create + bind the pepper:
  `head -c 32 /dev/urandom | base64 | tr -d '\n' | gcloud secrets create password-pepper --data-file=-`,
  grant the Cloud Run SA `secretAccessor`, then
  `gcloud run deploy … --update-secrets PASSWORD_PEPPER=password-pepper:latest`.
- README: an "Account password" section (pepper secret + claim-on-first-login).
  CHANGELOG entry; move the roadmap item to "Recently shipped".

## Verification
- `mvn test` (PasswordHasherTest + UserTest) and the full Playwright suite green
  (build-fresh harness).
- Manual prod smoke after deploy: existing accounts (`pdunham`, `dunhamrc`)
  **claim** their password on next login; a wrong password is rejected; a new name
  registers; `/hello/addartist` with a spoofed `lastfmName` no longer mutates.

## Notes / risks
- **Claim window** (accepted tradeoff): until each existing account logs in once,
  anyone could claim its password. Mitigate by claiming the known accounts
  immediately after deploy (or add a token-gated admin pre-seed later).
- **In-memory sessions across Cloud Run instances** (existing tech-debt): login
  state lives in one instance's session; multi-instance scaling can drop it. Fine
  at current low/single-instance traffic; a shared session store / stateless
  tokens is a separate item.
- **No self-serve recovery yet** — manual admin reset; email + reset flow deferred.
- Minor user-enumeration via login messaging; generic error text used, not
  hardened further for this small app.
- PBKDF2 cost (~210k iters) per login is fine at this traffic.
