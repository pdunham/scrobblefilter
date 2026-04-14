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
    // 2. Cloud Datastore emulator — replaces real Datastore during tests
    //    Prerequisite: gcloud SDK installed with the datastore emulator component
    {
      command: 'gcloud --quiet beta emulators datastore start --host-port=localhost:8081',
      port: 8081,
      reuseExistingServer: true,
    },
    // 3. App in Docker — must be pre-built: docker build -t scrobblefilter .
    //    DATASTORE_EMULATOR_HOST and LASTFM_BASE_URL redirect the app to local stubs
    {
      command: [
        'docker run --rm -p 8080:8080',
        '-e DATASTORE_EMULATOR_HOST=host.docker.internal:8081',
        '-e LASTFM_BASE_URL=http://host.docker.internal:9090/2.0/?',
        '-e GOOGLE_CLOUD_PROJECT=scrobblefilter',
        'scrobblefilter',
      ].join(' '),
      port: 8080,
      reuseExistingServer: true,
      timeout: 60_000, // Docker pull + container startup can be slow
    },
  ],
  projects: [{ name: 'chromium', use: { browserName: 'chromium' } }],
});
