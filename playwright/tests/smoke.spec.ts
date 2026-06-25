import { test, expect } from '@playwright/test';

test('root redirects to welcome page', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/hello\/welcome/);
});

test('welcome page shows Sign in with Last.fm link', async ({ page }) => {
  await page.goto('/hello/welcome');
  await expect(page.locator('a[href*="lastfm/signin"]')).toBeVisible();
  await expect(page.locator('body')).toContainText('Sign in with Last.fm');
});

test('world page without session redirects to welcome', async ({ page }) => {
  await page.goto('/hello/world');
  await expect(page).toHaveURL(/\/hello\/welcome/);
});
