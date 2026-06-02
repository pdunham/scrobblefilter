# Mission

> **Status:** Draft. This is the application's "constitution" — the stable
> statement of what ScrobbleFilter is for and the principles that govern how it
> evolves. Change it deliberately, not casually.

## What ScrobbleFilter is

ScrobbleFilter turns a person's listening history into a short, human social
post. A user connects their Last.fm account and a social account, curates a
list of artists they'd rather not broadcast, and the app periodically posts a
summary of who they've actually been listening to — minus the filtered ones:

> "I've been listening to *Artist 1*, *Artist 2*, and *Artist 3*."

## Who it's for

Music listeners who want a low-effort, automated way to share their taste
without (a) sharing *every* play and (b) without revealing guilty pleasures or
artists they don't want associated with their public profile. The filter list
is the product's reason to exist — anyone can auto-post their top artists; the
value here is *editorial control* over what gets shared.

## What it does today

- **Connect** a Last.fm identity and a Twitter/X account (OAuth 1.0a).
- **Curate** a per-user list of filtered artists that are excluded from posts.
- **Preview** the current weekly top artists on the dashboard, with filtered
  artists already removed.
- **Post** a weekly summary on an opt-in basis (the per-user `cron` flag), built
  from the Last.fm 7-day top-artists list.

## Principles

These are the commitments that should survive feature churn:

1. **The user owns the filter.** Filtering is the core promise. Posts must never
   include an artist the user has filtered. When in doubt, omit.
2. **Opt-in, not opt-out, for anything public.** Nothing is posted on a user's
   behalf unless they have explicitly enabled it. Linking an account is not
   consent to post.
3. **Identity is platform-independent.** A user is identified by their Last.fm
   name, not by the social platform they happen to post to. The data model
   treats the posting target as an attribute of the user, not their identity, so
   new platforms can be added without re-keying users.
4. **Least authority for secrets.** Credentials (Last.fm key, OAuth secrets,
   admin/cron tokens) live in a secret store and are injected at runtime, never
   committed to the repo or baked into source.
5. **Privileged endpoints are gated.** Admin and cron endpoints are not public
   surface area; they require a token the public cannot supply.
6. **Behaviour is verified, not assumed.** Changes that touch the post pipeline
   or the filter logic ship with an end-to-end test that exercises the real
   path.

## Explicit non-goals

- Not a full Last.fm client or analytics dashboard.
- Not a scrobbler — it reads listening data, it does not record plays.
- Not a general-purpose social scheduler — it posts one specific kind of update.

## Known constraints today

These are current limitations, not principles — see [roadmap.md](roadmap.md) for
the plan to lift them:

- Posts are hardcoded to **exactly three** artists.
- The Last.fm window is hardcoded to **7 days**.
- The only posting target is **Twitter/X**.
