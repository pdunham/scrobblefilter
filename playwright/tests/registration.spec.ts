import { test, expect } from '@playwright/test';

function uniqueHandle(): string {
  return `testuser_${Date.now()}`;
}

async function register(page: any, handle: string) {
  await page.goto('/hello/welcome');
  await page.fill('input[name="name"]', handle);
  await page.click('input[type="submit"]');
}

test('registering a new user shows dashboard with greeting', async ({ page }) => {
  const handle = uniqueHandle();
  await register(page, handle);
  await expect(page.locator('body')).toContainText(`Hello, ${handle}`);
});

test('dashboard shows unlinked Twitter account prompt for new user', async ({ page }) => {
  const handle = uniqueHandle();
  await register(page, handle);
  await expect(page.locator('body')).toContainText('You have not linked your twitter account');
});

test('dashboard shows Last.fm name input for new user', async ({ page }) => {
  const handle = uniqueHandle();
  await register(page, handle);
  await expect(page.locator('input[name="lastfmName"]')).toBeVisible();
});

test('registering the same handle twice loads the existing user', async ({ page }) => {
  const handle = uniqueHandle();
  await register(page, handle);
  await expect(page.locator('body')).toContainText(`Hello, ${handle}`);

  await register(page, handle);
  await expect(page.locator('body')).toContainText(`Hello, ${handle}`);
});

test('setting Last.fm name switches dashboard to add-artist form', async ({ page }) => {
  const handle = uniqueHandle();
  await register(page, handle);

  await page.fill('input[name="lastfmName"]', 'mylastfm');
  await page.click('input[type="submit"]');

  await expect(page.locator('body')).toContainText('mylastfm');
  await expect(page.locator('input[name="artist"]')).toBeVisible();
});

test('Twitter sign-in link is present and targets twittersignin endpoint', async ({ page }) => {
  const handle = uniqueHandle();
  await register(page, handle);

  const link = page.locator('a[href*="twittersignin"]');
  await expect(link).toBeVisible();
  const href = await link.getAttribute('href');
  expect(href).toContain('twittersignin');
  expect(href).toContain(handle);
});
