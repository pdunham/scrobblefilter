import { test, expect } from '@playwright/test';

// The AT Protocol OAuth client-metadata document. client_id must equal the URL
// it is served from and the redirect must share its origin; both are derived
// from the request.
test('client-metadata.json describes the OAuth public client', async ({ request }) => {
  const res = await request.get('/hello/client-metadata.json');
  expect(res.status()).toBe(200);
  expect(res.headers()['content-type']).toContain('application/json');

  const doc = await res.json();
  expect(doc.client_id).toMatch(/\/hello\/client-metadata\.json$/);
  expect(doc.redirect_uris).toContain(doc.client_id.replace('/hello/client-metadata.json', '/hello/bluesky/callback'));
  expect(doc.scope).toBe('atproto transition:generic');
  expect(doc.token_endpoint_auth_method).toBe('none');
  expect(doc.dpop_bound_access_tokens).toBe(true);
  expect(doc.grant_types).toEqual(expect.arrayContaining(['authorization_code', 'refresh_token']));
  expect(doc.response_types).toContain('code');
});
