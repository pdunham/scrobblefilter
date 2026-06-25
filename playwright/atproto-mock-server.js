// Minimal mock of the AT Protocol identity + OAuth surface, for the Bluesky
// connect-flow e2e. Started by Playwright (see playwright.config.ts).
//
// Docker networking split: the APP (in a container) reaches this mock via
// host.docker.internal, while the BROWSER (Playwright on the host) reaches it
// via localhost. So endpoints the app fetches advertise APP_BASE, but the
// authorization endpoint the browser is redirected to advertises BROWSER_BASE.
//
// DPoP/PKCE are intentionally not verified — this exercises the request/redirect
// shape of the flow; the proof and PKCE logic are unit-tested separately.
const http = require('http');
const url = require('url');

const PORT = 9091;
const APP_BASE = process.env.ATPROTO_MOCK_APP_BASE || 'http://host.docker.internal:9091';
const BROWSER_BASE = process.env.ATPROTO_MOCK_BROWSER_BASE || 'http://localhost:9091';

const parStore = {}; // request_uri -> { redirect_uri, state, loginHint }
let counter = 0;
let lastPost = null; // most recent createRecord payload (for e2e assertions)

// Test hook: a connected handle containing this marker has its refresh token
// rejected as invalid_grant, deterministically simulating an expired session.
// Keyed on the handle (threaded through the code + refresh token) rather than a
// global flag, so it's isolated under parallel workers sharing this one mock.
const EXPIRED_MARKER = 'expired';

function json(res, code, obj) {
  res.writeHead(code, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify(obj));
}

// The connecting handle is encoded after a '~' in both the auth code and the
// refresh token; pull it back out (empty string if absent/malformed).
function handleFromCode(code) {
  const i = (code || '').indexOf('~');
  return i < 0 ? '' : decodeURIComponent(code.slice(i + 1));
}
function handleFromRefresh(token) {
  const i = (token || '').indexOf('~');
  return i < 0 ? '' : decodeURIComponent(token.slice(i + 1));
}

const server = http.createServer((req, res) => {
  const u = url.parse(req.url, true);
  const path = u.pathname;

  // 1. handle -> DID
  if (path === '/xrpc/com.atproto.identity.resolveHandle') {
    return json(res, 200, { did: 'did:plc:mockuser' });
  }
  // 2. DID -> DID document (PLC), advertising the PDS (app-reachable)
  if (path === '/did:plc:mockuser') {
    return json(res, 200, {
      service: [{ id: '#atproto_pds', type: 'AtprotoPersonalDataServer', serviceEndpoint: APP_BASE }],
    });
  }
  // 3. PDS -> authorization server
  if (path === '/.well-known/oauth-protected-resource') {
    return json(res, 200, { authorization_servers: [APP_BASE] });
  }
  // 4. authorization server metadata. authorization_endpoint is browser-reachable.
  if (path === '/.well-known/oauth-authorization-server') {
    return json(res, 200, {
      issuer: APP_BASE,
      pushed_authorization_request_endpoint: APP_BASE + '/oauth/par',
      authorization_endpoint: BROWSER_BASE + '/oauth/authorize',
      token_endpoint: APP_BASE + '/oauth/token',
    });
  }
  // 5. PAR: remember redirect_uri + state keyed by a fresh request_uri.
  if (path === '/oauth/par' && req.method === 'POST') {
    let body = '';
    req.on('data', (c) => (body += c));
    req.on('end', () => {
      const params = new URLSearchParams(body);
      const requestUri = 'urn:mock:par:' + ++counter;
      parStore[requestUri] = {
        redirect_uri: params.get('redirect_uri'),
        state: params.get('state'),
        loginHint: params.get('login_hint') || '',
      };
      json(res, 201, { request_uri: requestUri, expires_in: 60 });
    });
    return;
  }
  // 6. authorize: redirect the browser back to the app with a code.
  if (path === '/oauth/authorize' && req.method === 'GET') {
    const stored = parStore[u.query.request_uri];
    if (!stored) {
      res.writeHead(400);
      return res.end('unknown request_uri');
    }
    // Carry the connecting handle in the code so the token exchange can mint a
    // refresh token that remembers which account it belongs to.
    const code = 'mock-code~' + encodeURIComponent(stored.loginHint || '');
    const loc =
      stored.redirect_uri +
      '?code=' + encodeURIComponent(code) + '&state=' + encodeURIComponent(stored.state) +
      '&iss=' + encodeURIComponent(APP_BASE);
    res.writeHead(302, { Location: loc });
    return res.end();
  }
  // 7. token: issue DPoP-bound tokens (refresh rotates the refresh token). The
  // handle is threaded through code → refresh token so a handle marked "expired"
  // gets its refresh rejected, deterministically simulating an expired session.
  if (path === '/oauth/token' && req.method === 'POST') {
    let body = '';
    req.on('data', (c) => (body += c));
    req.on('end', () => {
      const params = new URLSearchParams(body);
      const grant = params.get('grant_type');
      const handle = grant === 'refresh_token'
        ? handleFromRefresh(params.get('refresh_token'))
        : handleFromCode(params.get('code'));

      if (grant === 'refresh_token' && handle.includes(EXPIRED_MARKER)) {
        return json(res, 400, { error: 'invalid_grant', error_description: 'Session expired' });
      }
      json(res, 200, {
        access_token: 'mock-access-token',
        token_type: 'DPoP',
        refresh_token: 'mock-refresh~' + encodeURIComponent(handle),
        sub: 'did:plc:mockuser',
        scope: 'atproto transition:generic',
        expires_in: 3600,
      });
    });
    return;
  }
  // 8. PDS createRecord: record the post so the e2e can assert it.
  if (path === '/xrpc/com.atproto.repo.createRecord' && req.method === 'POST') {
    let body = '';
    req.on('data', (c) => (body += c));
    req.on('end', () => {
      try { lastPost = JSON.parse(body); } catch (_e) { lastPost = { raw: body }; }
      json(res, 200, { uri: 'at://did:plc:mockuser/app.bsky.feed.post/mockrkey', cid: 'mockcid' });
    });
    return;
  }
  // Debug: the most recent createRecord payload (test-only).
  if (path === '/debug/last-post' && req.method === 'GET') {
    return json(res, 200, lastPost || {});
  }

  res.writeHead(404);
  res.end('not found: ' + path);
});

server.listen(PORT, () => console.log(`AT Protocol mock listening on http://localhost:${PORT}`));
