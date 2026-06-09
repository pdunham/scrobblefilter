import { test, expect } from '@playwright/test';

test('root redirects to welcome page', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/hello\/welcome/);
});

test('welcome page renders registration form', async ({ page }) => {
  await page.goto('/hello/welcome');
  await expect(page.locator('input[name="lastfmName"]')).toBeVisible();
  await expect(page.locator('input[name="password"]')).toBeVisible();
  await expect(page.locator('input[type="submit"]')).toBeVisible();
  await expect(page.locator('body')).toContainText('Last.fm username');
});

test('world page without session redirects to welcome', async ({ page }) => {
  await page.goto('/hello/world');
  await expect(page).toHaveURL(/\/hello\/welcome/);
});
