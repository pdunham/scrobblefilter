import { test, expect } from '@playwright/test';
import { oauthLogin } from './helpers';

const MOCK = 'http://localhost:9091';

function uniqueLastfm(): string {
  return `lastfm_manual_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

test('"post to bluesky" on the filtered list posts immediately', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);

  // Connect Bluesky (mock OAuth). Note: no cron opt-in — manual posting ignores it.
  await page.goto('/hello/bluesky/signin?handle=alice.test');
  await expect(page.locator('body')).toContainText('@alice.test');

  // The filtered list offers a per-platform "post to bluesky" action.
  await page.goto(`/hello/filter?lastfmName=${lastfm}`);
  const link = page.locator('a[href="post?platform=bluesky"]');
  await expect(link).toBeVisible();
  await link.click();

  // It redirects back to the filtered list (no error) and the mock PDS got the post.
  await expect(page).toHaveURL(/\/hello\/filter/);
  await expect(page.locator('.error')).toHaveCount(0);

  const lastPost = await (await request.get(`${MOCK}/debug/last-post`)).json();
  expect(lastPost.collection).toBe('app.bsky.feed.post');
  expect(lastPost.record['$type']).toBe('app.bsky.feed.post');
  expect(lastPost.record.text).toContain("I've been listening to");
});

test('an expired Bluesky session clears the connection and prompts reconnect', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await oauthLogin(page, request, lastfm);

  // Connect a Bluesky account whose session the mock will reject on refresh
  // (the "expired" marker in the handle), and opt in to weekly posting.
  await page.goto('/hello/bluesky/signin?handle=expired.test');
  await expect(page.locator('body')).toContainText('@expired.test');
  await page.click('input[name="blueskyCron"]');
  await expect(page.locator('input[name="blueskyCron"]')).toBeChecked();
  await expect(page.locator('body')).toContainText('You have linked your Bluesky account');
  await expect(page.locator('.needs-reconnect')).toHaveCount(0);

  // Attempt to post; the refresh is rejected as invalid_grant.
  await page.goto(`/hello/filter?lastfmName=${lastfm}`);
  await page.locator('a[href="post?platform=bluesky"]').click();
  await expect(page).toHaveURL(/\/hello\/filter/);
  await expect(page.locator('.error')).toHaveCount(1); // the post failed

  // The expired session is now cleared: the dashboard shows a reconnect prompt,
  // no longer claims the account is connected, and keeps the weekly toggle on.
  await page.goto('/hello/world');
  await expect(page.locator('.needs-reconnect')).toContainText('expired');
  await expect(page.locator('.needs-reconnect')).toContainText('reconnect');
  await expect(page.locator('body')).not.toContainText('You have linked your Bluesky account');
  await expect(page.locator('input[name="blueskyCron"]')).toBeChecked();

  // The filtered list no longer offers a doomed "post to bluesky".
  await page.goto(`/hello/filter?lastfmName=${lastfm}`);
  await expect(page.locator('a[href="post?platform=bluesky"]')).toHaveCount(0);
});
