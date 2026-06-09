import { test, expect } from '@playwright/test';

function uniqueLastfm(): string {
  return `lastfm_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

const PASSWORD = 'test-pass-123';

async function register(page: any, lastfmName: string, password: string = PASSWORD) {
  await page.goto('/hello/welcome');
  await page.fill('input[name="lastfmName"]', lastfmName);
  await page.fill('input[name="password"]', password);
  await page.click('input[type="submit"]');
}

test('registering a new user shows dashboard with greeting', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await register(page, lastfm);
  // No Twitter handle at registration, so the greeting uses the Last.fm name.
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('dashboard shows unlinked Twitter account prompt for new user', async ({ page }) => {
  await register(page, uniqueLastfm());
  await expect(page.locator('body')).toContainText('You have not linked your twitter account');
});

test('dashboard shows Last.fm name after registration', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await register(page, lastfm);
  await expect(page.locator('body')).toContainText(lastfm);
});

test('registration without Last.fm name shows error', async ({ page }) => {
  await page.goto('/hello/welcome');
  // The Last.fm input has the required attribute; submit should not navigate.
  // Bypass HTML validation by submitting via JS so we exercise the server-side check.
  await page.evaluate(() => {
    const form = document.querySelector('form') as HTMLFormElement;
    form.noValidate = true;
    form.submit();
  });
  await expect(page.locator('body')).toContainText('Last.fm username is required');
});

test('registering the same Last.fm name twice loads the existing user', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await register(page, lastfm);
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);

  await register(page, lastfm);
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('add-artist form is visible immediately after registration', async ({ page }) => {
  await register(page, uniqueLastfm());
  await expect(page.locator('input[name="artist"]')).toBeVisible();
});

test('Twitter sign-in link is present and targets twittersignin endpoint', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await register(page, lastfm);

  const link = page.locator('a[href*="twittersignin"]');
  await expect(link).toBeVisible();
  const href = await link.getAttribute('href');
  expect(href).toContain('twittersignin');
  expect(href).toContain(lastfm);
});
