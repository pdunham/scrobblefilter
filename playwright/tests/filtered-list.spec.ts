// These tests require the app to be started with:
//   LASTFM_BASE_URL=http://host.docker.internal:9090/2.0/?
// Playwright's webServer config starts lastfm-mock-server.js on port 9090,
// which serves playwright/fixtures/lastfm-top-artists.json for every request.

import { test, expect } from '@playwright/test';

function uniqueHandle(): string {
  return `testuser_${Date.now()}`;
}

function uniqueLastfm(): string {
  return `lastfm_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

async function setupUser(page: any, handle: string, lastfm: string): Promise<void> {
  await page.goto('/hello/welcome');
  await page.fill('input[name="lastfmName"]', lastfm);
  await page.click('input[type="submit"]');
}

test('filtered list page shows top artists from the mock', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await setupUser(page, uniqueHandle(), lastfm);

  await page.goto(`/hello/filter?lastfmName=${lastfm}`);

  await expect(page.locator('table')).toContainText('Radiohead');
  await expect(page.locator('table')).toContainText('Portishead');
});

test('filtered artist is excluded from the list', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await setupUser(page, uniqueHandle(), lastfm);

  // Add Radiohead to the filter via the dashboard (target the add-artist form's
  // submit specifically; the dashboard has other forms such as connect Bluesky).
  await page.fill('input[name="artist"]', 'Radiohead');
  await page.click('form[action="addartist"] input[type="submit"]');

  await page.goto(`/hello/filter?lastfmName=${lastfm}`);

  await expect(page.locator('table')).not.toContainText('Radiohead');
  // Other artists from the fixture should still appear
  await expect(page.locator('table')).toContainText('Portishead');
});

test('"filter this artist" button adds artist and returns to dashboard', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await setupUser(page, uniqueHandle(), lastfm);

  await page.goto(`/hello/filter?lastfmName=${lastfm}`);

  // Click "filter this artist" for the first artist (Radiohead from fixture)
  await page.locator('input[value="filter this artist"]').first().click();

  // Should return to dashboard with artist now in filter list
  await expect(page.locator('body')).toContainText('Hello');
  await expect(page.locator('table').last()).toContainText('Radiohead');
});

test('"go back" link navigates to the dashboard', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await setupUser(page, uniqueHandle(), lastfm);

  await page.goto(`/hello/filter?lastfmName=${lastfm}`);
  await page.click('a[href="world"]');

  await expect(page).toHaveURL(/\/hello\/world/);
});
