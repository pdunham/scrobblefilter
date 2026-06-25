import { Page } from '@playwright/test';

/**
 * Sign in via the Last.fm OAuth mock flow.
 *
 * Calls /auth/setup to pre-register the username with a unique token (so the
 * mock's auth.getSession handler can look it up), then navigates directly to
 * the callback URL carrying that token. This skips the Last.fm auth-page
 * redirect entirely, which is irrelevant to what the tests are verifying and
 * was the source of a parallel-worker race condition when multiple workers
 * shared the mock's FIFO queue.
 */
export async function oauthLogin(page: Page, request: any, lastfm: string): Promise<void> {
  const res = await request.get(`http://localhost:9090/auth/setup?username=${lastfm}`);
  const { token } = await res.json();
  await page.goto(`/hello/lastfm/callback?token=${token}`);
  await page.waitForURL(/\/hello\/world/);
}
