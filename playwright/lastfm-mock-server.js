// Minimal HTTP server that returns the Last.fm fixture for any request.
// Started automatically by Playwright via the webServer config in playwright.config.ts.
// The app must be started with LASTFM_BASE_URL=http://host.docker.internal:9090/2.0/?
// so it hits this server instead of the real Last.fm API.
const http = require('http');
const fs = require('fs');
const path = require('path');

const fixture = fs.readFileSync(
  path.join(__dirname, 'fixtures', 'lastfm-top-artists.json'),
  'utf-8'
);

const PORT = 9090;

http.createServer((_req, res) => {
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(fixture);
}).listen(PORT, () => {
  console.log(`Last.fm mock server listening on http://localhost:${PORT}`);
});
