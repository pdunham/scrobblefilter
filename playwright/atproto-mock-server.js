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

const parStore = {}; // request_uri -> { redirect_uri, state }
let counter = 0;

function json(res, code, obj) {
  res.writeHead(code, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify(obj));
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
      parStore[requestUri] = { redirect_uri: params.get('redirect_uri'), state: params.get('state') };
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
    const loc =
      stored.redirect_uri +
      '?code=mock-code&state=' + encodeURIComponent(stored.state) +
      '&iss=' + encodeURIComponent(APP_BASE);
    res.writeHead(302, { Location: loc });
    return res.end();
  }
  // 7. token: issue DPoP-bound tokens.
  if (path === '/oauth/token' && req.method === 'POST') {
    return json(res, 200, {
      access_token: 'mock-access-token',
      token_type: 'DPoP',
      refresh_token: 'mock-refresh-token',
      sub: 'did:plc:mockuser',
      scope: 'atproto transition:generic',
      expires_in: 3600,
    });
  }

  res.writeHead(404);
  res.end('not found: ' + path);
});

server.listen(PORT, () => console.log(`AT Protocol mock listening on http://localhost:${PORT}`));
