import { test, expect } from '@playwright/test';

function uniqueHandle(): string {
  return `testuser_${Date.now()}`;
}

function uniqueLastfm(): string {
  return `lastfm_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

async function setupUser(page: any, handle: string, lastfm: string) {
  await page.goto('/hello/welcome');
  await page.fill('input[name="lastfmName"]', lastfm);
  await page.click('input[type="submit"]');
  // Now on dashboard with add-artist form visible
}

async function addArtist(page: any, artist: string) {
  await page.fill('input[name="artist"]', artist);
  // Target the add-artist form's submit specifically — the dashboard has other
  // forms (e.g. connect Bluesky), so "the first submit on the page" is ambiguous.
  await page.click('form[action="addartist"] input[type="submit"]');
}

// The filtered artists table is the second table on the page;
// the first table is the add-artist form layout table.
function filteredArtistsTable(page: any) {
  return page.locator('table').last();
}

test('adding an artist shows it in the filtered artists table', async ({ page }) => {
  await setupUser(page, uniqueHandle(), uniqueLastfm());
  await addArtist(page, 'Radiohead');
  await expect(filteredArtistsTable(page)).toContainText('Radiohead');
});

test('removing an artist removes it from the filtered artists table', async ({ page }) => {
  await setupUser(page, uniqueHandle(), uniqueLastfm());
  await addArtist(page, 'Radiohead');
  await expect(filteredArtistsTable(page)).toContainText('Radiohead');

  await page.click('a[href*="removeartist"]');

  await expect(page.locator('body')).not.toContainText('Radiohead');
});

test('removing an artist whose name contains an ampersand works', async ({ page }) => {
  // Regression: the remove link embeds the artist name (id = lastfm:name).
  // An unescaped "&" splits the query string, so removal silently failed.
  // The JSP now URL-encodes the id; this verifies the round-trip.
  await setupUser(page, uniqueHandle(), uniqueLastfm());
  await addArtist(page, 'Florence & the Machine');
  await expect(filteredArtistsTable(page)).toContainText('Florence & the Machine');

  await page.click('a[href*="removeartist"]');

  await expect(page.locator('body')).not.toContainText('Florence & the Machine');
});

test('adding multiple artists shows all of them in the table', async ({ page }) => {
  await setupUser(page, uniqueHandle(), uniqueLastfm());

  for (const artist of ['Radiohead', 'Portishead', 'Massive Attack']) {
    await addArtist(page, artist);
  }

  const table = filteredArtistsTable(page);
  await expect(table).toContainText('Radiohead');
  await expect(table).toContainText('Portishead');
  await expect(table).toContainText('Massive Attack');
});

test('cron toggle changes cron state on the dashboard', async ({ page }) => {
  await setupUser(page, uniqueHandle(), uniqueLastfm());

  // New user has cron=false; checkbox should be unchecked
  await expect(page.locator('input[type="checkbox"]')).not.toBeChecked();

  // Submit button has value="true" — clicking it enables cron
  await page.click('input[name="cron"]');

  // After enabling, checkbox should be checked
  await expect(page.locator('input[type="checkbox"]')).toBeChecked();
});

test('filtered list link is visible after registration', async ({ page }) => {
  await setupUser(page, uniqueHandle(), uniqueLastfm());
  await expect(page.locator('a[href*="filter"]')).toBeVisible();
});
