import { test, expect } from '@playwright/test';
import { oauthLogin } from './helpers';

function uniqueLastfm(): string {
  return `lastfm_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

async function setupUser(page: any, request: any, lastfm: string) {
  await oauthLogin(page, request, lastfm);
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

test('adding an artist shows it in the filtered artists table', async ({ page, request }) => {
  await setupUser(page, request, uniqueLastfm());
  await addArtist(page, 'Radiohead');
  await expect(filteredArtistsTable(page)).toContainText('Radiohead');
});

test('removing an artist removes it from the filtered artists table', async ({ page, request }) => {
  await setupUser(page, request, uniqueLastfm());
  await addArtist(page, 'Radiohead');
  await expect(filteredArtistsTable(page)).toContainText('Radiohead');

  await page.click('a[href*="removeartist"]');

  await expect(page.locator('body')).not.toContainText('Radiohead');
});

test('removing an artist whose name contains an ampersand works', async ({ page, request }) => {
  // Regression: the remove link embeds the artist name (id = lastfm:name).
  // An unescaped "&" splits the query string, so removal silently failed.
  // The JSP now URL-encodes the id; this verifies the round-trip.
  await setupUser(page, request, uniqueLastfm());
  await addArtist(page, 'Florence & the Machine');
  await expect(filteredArtistsTable(page)).toContainText('Florence & the Machine');

  await page.click('a[href*="removeartist"]');

  await expect(page.locator('body')).not.toContainText('Florence & the Machine');
});

test('adding multiple artists shows all of them in the table', async ({ page, request }) => {
  await setupUser(page, request, uniqueLastfm());

  for (const artist of ['Radiohead', 'Portishead', 'Massive Attack']) {
    await addArtist(page, artist);
  }

  const table = filteredArtistsTable(page);
  await expect(table).toContainText('Radiohead');
  await expect(table).toContainText('Portishead');
  await expect(table).toContainText('Massive Attack');
});

test('weekly-post toggles are disabled until the account is linked', async ({ page, request }) => {
  await setupUser(page, request, uniqueLastfm());

  // A brand-new user has neither Twitter nor Bluesky linked, so both weekly
  // toggles are present but greyed out (disabled) and off.
  await expect(page.locator('input[name="cron"]')).toBeDisabled();
  await expect(page.locator('input[name="cron"]')).not.toBeChecked();
  await expect(page.locator('input[name="blueskyCron"]')).toBeDisabled();
  await expect(page.locator('input[name="blueskyCron"]')).not.toBeChecked();
});

test('filtered list link is visible after registration', async ({ page, request }) => {
  await setupUser(page, request, uniqueLastfm());
  await expect(page.locator('a[href*="filter"]')).toBeVisible();
});
