import { test, expect } from '@playwright/test';
import { oauthLogin } from './helpers';

function uniqueLastfm(): string {
  return `lastfm_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

test('registering a new user shows dashboard with greeting', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('dashboard shows unlinked Twitter account prompt for new user', async ({ page, request }) => {
  await oauthLogin(page, request, uniqueLastfm());
  await expect(page.locator('body')).toContainText('You have not linked your twitter account');
});

test('dashboard shows Last.fm name after registration', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);
  await expect(page.locator('body')).toContainText(lastfm);
});

test('registering the same Last.fm name twice loads the existing user', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);

  await page.goto('/hello/logout');
  await oauthLogin(page, request, lastfm);
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('add-artist form is visible immediately after registration', async ({ page, request }) => {
  await oauthLogin(page, request, uniqueLastfm());
  await expect(page.locator('input[name="artist"]')).toBeVisible();
});

test('Twitter sign-in link is present and targets twittersignin endpoint', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);

  const link = page.locator('a[href*="twittersignin"]');
  await expect(link).toBeVisible();
  const href = await link.getAttribute('href');
  expect(href).toContain('twittersignin');
  expect(href).toContain(lastfm);
});
