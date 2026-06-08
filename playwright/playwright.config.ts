import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  reporter: [
    ['list'],                                      // terminal output
    ['html', { outputFolder: 'playwright-report', open: 'never' }], // saved report
  ],
  use: {
    baseURL: 'http://localhost:8080',
  },
  // All three servers are started in parallel. Playwright waits for each port
  // to be ready before running any tests, so by the time tests start all
  // services are up — regardless of which one finishes starting first.
  // reuseExistingServer means already-running instances are reused (dev workflow).
  webServer: [
    // 1. Last.fm mock — serves fixtures/lastfm-top-artists.json for every request
    {
      command: 'node lastfm-mock-server.js',
      port: 9090,
      reuseExistingServer: true,
    },
    // 1b. AT Protocol mock — identity resolution + OAuth (PAR/authorize/token)
    {
      command: 'node atproto-mock-server.js',
      port: 9091,
      reuseExistingServer: true,
    },
    // 2. Cloud Datastore emulator — replaces real Datastore during tests
    //    Prerequisite: gcloud SDK installed with the datastore emulator component
    //    --consistency=1.0 makes all queries strongly consistent (matches modern
    //    Firestore-in-Datastore-mode production behavior; default 0.9 introduces
    //    flaky read-your-writes failures in tests that exercise non-ancestor queries).
    {
      command: 'gcloud --quiet beta emulators datastore start --host-port=0.0.0.0:8081 --consistency=1.0',
      port: 8081,
      reuseExistingServer: true,
    },
    // 3. App in Docker. The image is rebuilt from current source by the `pretest`
    //    npm hook (which also removes any leftover scrobblefilter-e2e container),
    //    so this is a single directly-spawned `docker run` that Playwright can
    //    cleanly stop on teardown. reuseExistingServer:false guarantees the suite
    //    runs against the freshly built image, never a stale container left on
    //    :8080. DATASTORE_EMULATOR_HOST and LASTFM_BASE_URL redirect to local stubs.
    {
      command: [
        'docker run --rm --name scrobblefilter-e2e -p 8080:8080',
        '-e DATASTORE_EMULATOR_HOST=http://host.docker.internal:8081',
        '-e LASTFM_BASE_URL=http://host.docker.internal:9090/2.0/?',
        '-e GOOGLE_CLOUD_PROJECT=scrobblefilter',
        // Test-only AES-256 key (base64 32 bytes) for CredentialCrypto; never used in prod.
        '-e CRED_ENC_KEY=JfDfFR5fa0QxCKSTR8S8JJonXCQPRdXTG/5G+dqYHs4=',
        // Point AT Protocol resolution at the mock (app reaches host via host.docker.internal).
        '-e BLUESKY_HANDLE_RESOLVER_URL=http://host.docker.internal:9091',
        '-e BLUESKY_PLC_DIRECTORY_URL=http://host.docker.internal:9091',
        // Test cron token so the e2e can trigger the weekly post endpoint.
        '-e CRON_TOKEN=test-cron-token',
        'scrobblefilter',
      ].join(' '),
      // Use url (not port) so Playwright waits for the app to be fully
      // deployed, not just for Tomcat's TCP port to open.
      url: 'http://localhost:8080/hello/welcome',
      reuseExistingServer: false,
      timeout: 90_000,
    },
  ],
  projects: [{ name: 'chromium', use: { browserName: 'chromium' } }],
});
