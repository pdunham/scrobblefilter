// Mock server for Last.fm API and Web Auth flows.
// Started automatically by Playwright via the webServer config in playwright.config.ts.
// The app must be started with:
//   LASTFM_BASE_URL=http://host.docker.internal:9090/2.0/?
//   LASTFM_AUTH_URL=http://localhost:9090/auth/
//
// The auth-page redirect (/auth/) is intercepted by Playwright in oauthLogin()
// so the browser never actually hits this server for that step. The server only
// needs to handle auth.getSession (server-side from the app inside Docker).
const http = require('http');
const fs = require('fs');
const path = require('path');
const { URL } = require('url');

const fixture = fs.readFileSync(
  path.join(__dirname, 'fixtures', 'lastfm-top-artists.json'),
  'utf-8'
);

const PORT = 9090;

// token -> username; populated by /auth/setup and queried by auth.getSession.
const tokenStore = new Map();
let tokenCounter = 0;

http.createServer((req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);

  // Pre-register a username for the next OAuth flow; returns { token }.
  if (url.pathname === '/auth/setup') {
    const username = url.searchParams.get('username') || 'unknown';
    const token = `tok-${++tokenCounter}`;
    tokenStore.set(token, username);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ token }));
    return;
  }

  // auth.getSession: look up the token and return the Last.fm session.
  if (url.searchParams.get('method') === 'auth.getSession') {
    const token = url.searchParams.get('token');
    const username = tokenStore.get(token) || 'unknown';
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ session: { name: username, key: 'mock-session-key', subscriber: 0 } }));
    return;
  }

  // Default: return the top-artists fixture for user.gettopartists calls.
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(fixture);
}).listen(PORT, () => {
  console.log(`Last.fm mock server listening on http://localhost:${PORT}`);
});
