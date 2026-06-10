import { test, expect } from '@playwright/test';

function uniqueLastfm(): string {
  return `lastfm_auth_${Date.now()}_${Math.floor(Math.random() * 10000)}`;
}

async function submitLogin(page: any, lastfm: string, password: string) {
  await page.goto('/hello/welcome');
  await page.fill('input[name="lastfmName"]', lastfm);
  await page.fill('input[name="password"]', password);
  await page.click('input[type="submit"]');
}

test('new user registers with a password and lands on the dashboard', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await submitLogin(page, lastfm, 'sekret-1');
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('returning user with the correct password logs in', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await submitLogin(page, lastfm, 'sekret-1');           // first visit sets the password
  await page.goto('/hello/logout');
  await submitLogin(page, lastfm, 'sekret-1');           // log back in
  await expect(page.locator('body')).toContainText(`Hello, ${lastfm}`);
});

test('wrong password is rejected and no session is established', async ({ page }) => {
  const lastfm = uniqueLastfm();
  await submitLogin(page, lastfm, 'sekret-1');           // set the password
  await page.goto('/hello/logout');

  await submitLogin(page, lastfm, 'WRONG');              // try a bad password
  await expect(page.locator('body')).toContainText("don't match");
  await expect(page.locator('body')).not.toContainText(`Hello, ${lastfm}`);

  // No session: the dashboard bounces back to the welcome page.
  await page.goto('/hello/world');
  await expect(page).toHaveURL(/\/hello\/welcome/);
});

test('registration without a password shows an error', async ({ page }) => {
  await page.goto('/hello/welcome');
  await page.fill('input[name="lastfmName"]', uniqueLastfm());
  // Bypass HTML "required" to exercise the server-side check.
  await page.evaluate(() => {
    const form = document.querySelector('form') as HTMLFormElement;
    form.noValidate = true;
    form.submit();
  });
  await expect(page.locator('body')).toContainText('password is required');
});

test('addartist cannot mutate another account without a session (IDOR)', async ({ page, request }) => {
  // Victim registers and adds nothing; attacker (no session) POSTs addartist
  // with the victim's lastfmName. It must not add the artist to the victim.
  const victim = uniqueLastfm();
  await submitLogin(page, victim, 'sekret-1');
  await page.goto('/hello/logout');

  const res = await request.post('/hello/addartist', {
    form: { lastfmName: victim, artist: 'Sneaky Inserted Artist' },
    maxRedirects: 0,
  });
  // No session -> redirected to welcome, not the dashboard.
  expect(res.status()).toBeGreaterThanOrEqual(300);
  expect(res.status()).toBeLessThan(400);
  expect(res.headers()['location']).toContain('/hello/welcome');

  // Log the victim back in; the artist must NOT be present.
  await submitLogin(page, victim, 'sekret-1');
  await expect(page.locator('body')).not.toContainText('Sneaky Inserted Artist');
});
