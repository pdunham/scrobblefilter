import { test, expect } from '@playwright/test';
import { oauthLogin } from './helpers';

const CRON_TOKEN = 'test-cron-token';
const MOCK = 'http://localhost:9091';

function uniqueLastfm(): string {
  return `lastfm_cron_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

test('weekly cron fans out a post to Bluesky for an opted-in connected user', async ({ page, request }) => {
  // Register + connect Bluesky (mock OAuth).
  await oauthLogin(page, request, uniqueLastfm());

  await page.goto('/hello/bluesky/signin?handle=alice.test');
  await expect(page.locator('body')).toContainText('@alice.test');

  // Opt in to weekly Bluesky posting.
  await page.click('input[name="blueskyCron"]');
  await expect(page.locator('form[action="updateBlueskyCronSetting"] input[type="checkbox"]')).toBeChecked();

  // Trigger the weekly cron endpoint (token-gated).
  const cron = await request.get('/hello/cron/sendalltweets', { headers: { 'X-Cron-Token': CRON_TOKEN } });
  expect(cron.status()).toBe(200);

  // The mock PDS received a createRecord for an app.bsky.feed.post with the summary.
  const lastPost = await (await request.get(`${MOCK}/debug/last-post`)).json();
  expect(lastPost.collection).toBe('app.bsky.feed.post');
  expect(lastPost.repo).toBe('did:plc:mockuser');
  expect(lastPost.record['$type']).toBe('app.bsky.feed.post');
  expect(lastPost.record.text).toContain("I've been listening to");
});
