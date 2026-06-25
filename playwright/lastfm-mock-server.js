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

  // Browser-facing Web Auth page: mint a token for a (default) user and
  // redirect back to the app's callback, mimicking Last.fm's redirect. This
  // lets the "Sign in with Last.fm" button work in local manual testing; the
  // e2e tests bypass this and drive the callback directly (see helpers.ts).
  if (url.pathname === '/auth/' || url.pathname === '/auth') {
    const cb = url.searchParams.get('cb');
    if (!cb) {
      res.writeHead(400);
      res.end('missing cb');
      return;
    }
    const username = url.searchParams.get('username') || 'musicfan';
    const token = `tok-${++tokenCounter}`;
    tokenStore.set(token, username);
    const sep = cb.includes('?') ? '&' : '?';
    res.writeHead(302, { Location: `${cb}${sep}token=${token}` });
    res.end();
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
