import { test, expect } from '@playwright/test';

async function setupUser(page: any, handle: string) {
  await page.goto('/hello/welcome');
  await page.fill('input[name="name"]', handle);
  await page.click('input[type="submit"]');
  await page.fill('input[name="lastfmName"]', 'mylastfm');
  await page.click('input[type="submit"]');
  // Now on dashboard with add-artist form visible
}

async function addArtist(page: any, artist: string) {
  await page.fill('input[name="artist"]', artist);
  await page.click('input[type="submit"]');
}

test('adding an artist shows it in the filtered artists table', async ({ page }) => {
  const handle = `testuser_${Date.now()}`;
  await setupUser(page, handle);
  await addArtist(page, 'Radiohead');
  await expect(page.locator('table')).toContainText('Radiohead');
});

test('removing an artist removes it from the filtered artists table', async ({ page }) => {
  const handle = `testuser_${Date.now()}`;
  await setupUser(page, handle);
  await addArtist(page, 'Radiohead');
  await expect(page.locator('table')).toContainText('Radiohead');

  await page.click('a[href*="removeartist"]');

  await expect(page.locator('body')).not.toContainText('Radiohead');
});

test('adding multiple artists shows all of them in the table', async ({ page }) => {
  const handle = `testuser_${Date.now()}`;
  await setupUser(page, handle);

  for (const artist of ['Radiohead', 'Portishead', 'Massive Attack']) {
    await addArtist(page, artist);
  }

  const table = page.locator('table');
  await expect(table).toContainText('Radiohead');
  await expect(table).toContainText('Portishead');
  await expect(table).toContainText('Massive Attack');
});

test('cron toggle changes cron state on the dashboard', async ({ page }) => {
  const handle = `testuser_${Date.now()}`;
  await setupUser(page, handle);

  // New user has cron=false; checkbox should be unchecked
  await expect(page.locator('input[type="checkbox"]')).not.toBeChecked();

  // Submit button has value="true" — clicking it enables cron
  await page.click('input[name="cron"]');

  // After enabling, checkbox should be checked
  await expect(page.locator('input[type="checkbox"]')).toBeChecked();
});

test('filtered list link is visible after Last.fm name is set', async ({ page }) => {
  const handle = `testuser_${Date.now()}`;
  await setupUser(page, handle);
  await expect(page.locator('a[href*="filter"]')).toBeVisible();
});
