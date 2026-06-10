import { test, expect } from '@playwright/test';

function uniqueLastfm(): string {
  return `lastfm_bsky_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

async function register(page: any, lastfm: string) {
  await page.goto('/hello/welcome');
  await page.fill('input[name="lastfmName"]', lastfm);
  await page.fill('input[name="password"]', 'test-pass-123');
  await page.click('input[type="submit"]');
}

test('a new user sees the connect-Bluesky form with a disabled weekly toggle', async ({ page }) => {
  await register(page, uniqueLastfm());
  await expect(page.locator('body')).toContainText('not linked your Bluesky account');
  await expect(page.locator('input[name="handle"]')).toBeVisible();
  // The weekly toggle is shown but greyed out until Bluesky is linked.
  await expect(page.locator('input[name="blueskyCron"]')).toBeDisabled();
});

test('connecting Bluesky via OAuth persists the account', async ({ page }) => {
  await register(page, uniqueLastfm());

  // Drive the connect flow. The app resolves the handle (mock), pushes the
  // authorization request (mock PAR) and redirects to the mock authorize
  // endpoint, which redirects back to the callback with a code; the callback
  // exchanges it (mock token) and lands on the dashboard.
  await page.goto('/hello/bluesky/signin?handle=alice.test');

  await expect(page).toHaveURL(/\/hello\/world/);
  await expect(page.locator('body')).toContainText('linked your Bluesky account');
  await expect(page.locator('body')).toContainText('@alice.test');

  // Persisted: a fresh dashboard load still shows the linked handle.
  await page.goto('/hello/world');
  await expect(page.locator('body')).toContainText('@alice.test');
});
