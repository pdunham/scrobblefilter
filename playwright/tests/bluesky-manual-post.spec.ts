import { test, expect } from '@playwright/test';

const MOCK = 'http://localhost:9091';

function uniqueLastfm(): string {
  return `lastfm_manual_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

test('"post to bluesky" on the filtered list posts immediately', async ({ page, request }) => {
  const lastfm = uniqueLastfm();
  await page.goto('/hello/welcome');
  await page.fill('input[name="lastfmName"]', lastfm);
  await page.fill('input[name="name"]', 'manualbsky');
  await page.click('input[type="submit"]');

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
