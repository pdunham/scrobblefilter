import { test, expect } from '@playwright/test';
import { oauthLogin } from './helpers';

function uniqueLastfm(): string {
  return `lastfm_auth_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

test('signs in via Last.fm and lands on dashboard', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('returning user can sign in again', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);
  await page.goto('/hello/logout');
  await oauthLogin(page, request, lastfm);
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('addartist cannot mutate another account without a session (IDOR)', async ({ page, request }) => {
  // Victim signs in; attacker (no session) POSTs addartist with the victim's lastfmName.
  const victim = uniqueLastfm();
  await oauthLogin(page, request, victim);
  await page.goto('/hello/logout');

  const res = await request.post('/hello/addartist', {
    form: { lastfmName: victim, artist: 'Sneaky Inserted Artist' },
    maxRedirects: 0,
  });
  // No session -> redirected to welcome, not the dashboard.
  expect(res.status()).toBeGreaterThanOrEqual(300);
  expect(res.status()).toBeLessThan(400);
  expect(res.headers()['location']).toContain('/hello/welcome');

  // Log the victim back in; the artist must NOT be present.
  await oauthLogin(page, request, victim);
  await expect(page.locator('body')).not.toContainText('Sneaky Inserted Artist');
});
