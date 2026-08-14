'use client'

import Script from 'next/script'
import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react'
import { PageShell } from '@/shared/ui'
import { EnergyIcon, GemIcon, StreakIcon } from '@/shared/ui/icons'
import {
  ApiError,
  clearToken,
  getProfile,
  linkGuestProgress,
  storeToken,
  type Profile,
} from '@/shared/lib/api'
import {
  GOOGLE_CLIENT_ID,
  GOOGLE_SIGN_IN_ENABLED,
  GSI_SCRIPT_SRC,
  googleIdentity,
  nameFromIdToken,
  type GoogleCredentialResponse,
} from './googleIdentity'

function asCount(value: unknown): number {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0
}

function progressPercent(completed: number, total: number): number {
  if (total <= 0) return 0
  return Math.min(100, Math.round((completed / total) * 100))
}

function energyHint(stats: Profile['stats']): string {
  if (stats.energy >= stats.maxEnergy) return 'Full — ready for the next lesson'
  const minutes = Math.ceil(stats.secondsUntilNextEnergy / 60)
  return `${stats.energy} of ${stats.maxEnergy} — next point in about ${minutes} min`
}

function ProfileAvatar({
  pictureUrl,
  displayName,
}: {
  pictureUrl: string | null
  displayName: string | null
}) {
  const initial = (displayName?.trim().charAt(0) || '?').toUpperCase()

  if (pictureUrl) {
    return (
      // A 96px avatar already on Google's CDN. next/image would need Google in
      // remotePatterns and would proxy it through the optimiser — paying quota
      // to make a small image slower, and complicating the no-referrer policy
      // this needs to keep the account page out of Google's logs.
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={pictureUrl}
        alt={displayName ? `${displayName}'s profile photo` : 'Google profile photo'}
        width={96}
        height={96}
        referrerPolicy="no-referrer"
        style={{
          width: 'clamp(4.5rem, 8vw, 5.5rem)',
          height: 'clamp(4.5rem, 8vw, 5.5rem)',
          borderRadius: '50%',
          objectFit: 'cover',
          border: '2px solid var(--brand)',
          background: 'var(--bg-chip)',
          flexShrink: 0,
        }}
      />
    )
  }

  return (
    <div
      aria-hidden="true"
      style={{
        width: 'clamp(4.5rem, 8vw, 5.5rem)',
        height: 'clamp(4.5rem, 8vw, 5.5rem)',
        borderRadius: '50%',
        display: 'grid',
        placeItems: 'center',
        flexShrink: 0,
        border: '2px solid var(--brand)',
        background: 'linear-gradient(135deg, var(--unit-from), var(--unit-to))',
        color: 'var(--text)',
        fontFamily: 'var(--font-brand), Georgia, serif',
        fontSize: 'clamp(1.6rem, 3vw, 2.1rem)',
        fontWeight: 700,
      }}
    >
      {initial}
    </div>
  )
}

function StatRow({
  label,
  value,
  hint,
  color,
  icon,
}: {
  label: string
  value: string
  hint: string
  color: string
  icon: ReactNode
}) {
  return (
    <div
      style={{
        display: 'grid',
        gap: '0.25rem',
        padding: '0.9rem 0',
        borderBottom: '1px solid var(--line)',
      }}
    >
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'baseline',
          gap: '1rem',
          flexWrap: 'wrap',
        }}
      >
        <span style={{ color: 'var(--muted)', fontSize: '0.9rem', fontWeight: 700 }}>{label}</span>
        <span
          style={{
            color,
            fontWeight: 800,
            fontSize: '1.1rem',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
          }}
        >
          {icon}
          {value}
        </span>
      </div>
      <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.85rem', lineHeight: 1.45 }}>{hint}</p>
    </div>
  )
}

/**
 * Account screen: identity, lesson progress, and the rewards that come with it.
 * Guests can still see progress; signing in with Google unlocks email + photo.
 */
export function AccountPageView() {
  const [profile, setProfile] = useState<Profile | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [status, setStatus] = useState<string | null>(null)
  const [scriptReady, setScriptReady] = useState(false)
  const buttonHost = useRef<HTMLDivElement | null>(null)

  const refresh = useCallback(async () => {
    try {
      const next = await getProfile()
      setProfile(next)
      setError(null)
    } catch (err) {
      setProfile(null)
      setError(err instanceof ApiError ? err.message : 'Could not load your account.')
    }
  }, [])

  useEffect(() => {
    // Fetch-on-mount, not a synchronous setState cascade — the rule cannot see
    // the await. It has to happen here: the profile is keyed by a guest id in
    // localStorage, so this cannot move to the server.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void refresh()
  }, [refresh])

  const onCredential = useCallback(
    async (response: GoogleCredentialResponse) => {
      storeToken(response.credential)
      setStatus('Bringing your progress across…')
      try {
        const next = await linkGuestProgress()
        setProfile({
          ...next,
          displayName: next.displayName ?? nameFromIdToken(response.credential),
        })
        setStatus(null)
        setError(null)
      } catch (err) {
        clearToken()
        setStatus(null)
        setError(
          err instanceof ApiError ? err.message : 'Could not finish signing in. Please try again.',
        )
        void refresh()
      }
    },
    [refresh],
  )

  useEffect(() => {
    if (!scriptReady || profile?.signedIn || !buttonHost.current) return
    if (!GOOGLE_SIGN_IN_ENABLED) return
    const identity = googleIdentity()
    if (!identity) return

    identity.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: response => void onCredential(response),
      cancel_on_tap_outside: true,
    })
    identity.renderButton(buttonHost.current, {
      type: 'standard',
      theme: 'filled_black',
      size: 'large',
      shape: 'pill',
      text: 'continue_with',
      width: 320,
    })
  }, [scriptReady, profile?.signedIn, onCredential])

  function signOut() {
    googleIdentity()?.disableAutoSelect()
    clearToken()
    setStatus(null)
    void refresh()
  }

  const completed = asCount(profile?.completedLessons)
  const total = asCount(profile?.totalLessons)
  const percent = progressPercent(completed, total)
  const signedIn = profile?.signedIn === true

  return (
    <PageShell>
      {GOOGLE_SIGN_IN_ENABLED && <Script src={GSI_SCRIPT_SRC} onReady={() => setScriptReady(true)} />}

      <header style={{ marginBottom: '1.5rem' }} className="account-span">
        <p
          style={{
            margin: 0,
            color: 'var(--muted)',
            fontSize: '0.8rem',
            fontWeight: 700,
            letterSpacing: '0.06em',
            textTransform: 'uppercase',
          }}
        >
          Your place on the path
        </p>
        <h1 className="brand-font page-title" style={{ marginTop: '0.25rem' }}>
          Account
        </h1>
      </header>

      {error && (
        <p role="alert" style={{ color: 'var(--danger)', marginTop: 0 }} className="account-span">
          {error}
        </p>
      )}

      {!profile && !error && (
        <p style={{ color: 'var(--muted)' }} role="status" className="account-span">
          Loading your account…
        </p>
      )}

      {profile && (
        <div className="account-body">
          <section
            aria-label="Profile"
            className="account-span"
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              alignItems: 'center',
              gap: '1.2rem',
              padding: '1.2rem 1.15rem',
              borderRadius: 16,
              border: '1px solid var(--line)',
              background: 'var(--bg-elevated)',
            }}
          >
            <ProfileAvatar pictureUrl={profile.pictureUrl} displayName={profile.displayName} />
            <div style={{ flex: '1 1 14rem', minWidth: 0 }}>
              <p
                className="brand-font"
                style={{
                  margin: 0,
                  fontSize: 'clamp(1.3rem, 2vw + 0.8rem, 1.55rem)',
                  color: 'var(--text)',
                  overflowWrap: 'anywhere',
                }}
              >
                {signedIn ? profile.displayName || 'Signed in' : 'Learning as a guest'}
              </p>
              {signedIn && profile.email ? (
                <p
                  style={{
                    margin: '0.35rem 0 0',
                    color: 'var(--muted)',
                    fontSize: '1rem',
                    overflowWrap: 'anywhere',
                  }}
                >
                  {profile.email}
                </p>
              ) : (
                <p
                  style={{
                    margin: '0.35rem 0 0',
                    color: 'var(--muted)',
                    fontSize: '0.95rem',
                    lineHeight: 1.5,
                    maxWidth: '36rem',
                  }}
                >
                  Sign in with Google to save progress across devices and show your email here.
                </p>
              )}
              {signedIn && (
                <button
                  type="button"
                  onClick={signOut}
                  style={{
                    marginTop: '0.9rem',
                    border: '1px solid var(--line)',
                    background: 'transparent',
                    color: 'var(--muted)',
                    borderRadius: 999,
                    padding: '0.5rem 1.05rem',
                    fontWeight: 700,
                    fontSize: '0.9rem',
                  }}
                >
                  Sign out
                </button>
              )}
            </div>
          </section>

          {!signedIn && GOOGLE_SIGN_IN_ENABLED && (
            <section aria-label="Sign in" className="account-span" style={{ display: 'grid', gap: '0.7rem' }}>
              <h2 className="brand-font" style={{ margin: 0, fontSize: '1.2rem', color: 'var(--text)' }}>
                Keep learning on any device
              </h2>
              <p style={{ margin: 0, color: 'var(--muted)', lineHeight: 1.55, fontSize: '0.98rem', maxWidth: '40rem' }}>
                Everything you have already done as a guest comes with you. No lesson is lost when
                you sign in.
              </p>
              <div ref={buttonHost} style={{ marginTop: '0.35rem' }} />
            </section>
          )}

          {!GOOGLE_SIGN_IN_ENABLED && !signedIn && (
            <p className="account-span" style={{ margin: 0, color: 'var(--muted)', lineHeight: 1.55 }}>
              Google sign-in is not configured in this environment. You can still learn as a guest on
              this device.
            </p>
          )}

          {status && (
            <p role="status" className="account-span" style={{ margin: 0, color: 'var(--muted)' }}>
              {status}
            </p>
          )}

          <section aria-label="Lesson progress" className="account-panel">
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'baseline',
                gap: '0.75rem',
                flexWrap: 'wrap',
              }}
            >
              <h2 className="brand-font" style={{ margin: 0, fontSize: '1.2rem', color: 'var(--text)' }}>
                Path progress
              </h2>
              <span style={{ color: 'var(--brand)', fontWeight: 800 }}>
                {completed} / {total} lessons
              </span>
            </div>
            <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.95rem', lineHeight: 1.5 }}>
              {total === 0
                ? 'Lesson totals will appear once the curriculum has loaded.'
                : completed === 0
                  ? 'You have not finished a lesson yet — open the path and start with the first seal.'
                  : completed >= total
                    ? 'You have completed every lesson on the current path. Well done.'
                    : `You are ${percent}% through the published path.`}
            </p>
            <div
              role="progressbar"
              aria-valuemin={0}
              aria-valuemax={100}
              aria-valuenow={percent}
              aria-label="Lessons completed"
              style={{
                height: 12,
                borderRadius: 999,
                background: 'var(--bg-chip)',
                border: '1px solid var(--line)',
                overflow: 'hidden',
                marginTop: '0.35rem',
              }}
            >
              <div
                style={{
                  width: `${percent}%`,
                  height: '100%',
                  background: 'linear-gradient(90deg, var(--brand-deep), var(--brand))',
                  boxShadow: percent > 0 ? '0 0 12px rgba(196, 163, 90, 0.35)' : undefined,
                  transition: 'width 0.35s ease',
                }}
              />
            </div>
          </section>

          <section aria-label="Rewards and energy" className="account-panel">
            <h2 className="brand-font" style={{ margin: 0, fontSize: '1.2rem', color: 'var(--text)' }}>
              Rewards
            </h2>
            <StatRow
              label="Gems"
              value={String(profile.stats.gems)}
              icon={<GemIcon size={20} />}
              hint={
                profile.stats.gems === 0
                  ? 'Earn 10 gems the first time you finish a lesson.'
                  : 'Gems mark lessons you have finished for the first time.'
              }
              color="var(--gem)"
            />
            <StatRow
              label="Streak"
              value={`${profile.stats.streak} day${profile.stats.streak === 1 ? '' : 's'}`}
              icon={<StreakIcon size={20} />}
              hint={
                profile.stats.streak === 0
                  ? 'Complete a lesson today to start a streak.'
                  : 'Consecutive UTC days with at least one completed lesson.'
              }
              color="var(--streak)"
            />
            <StatRow
              label="Energy"
              value={String(profile.stats.energy)}
              icon={<EnergyIcon size={20} />}
              hint={energyHint(profile.stats)}
              color="var(--energy)"
            />
          </section>
        </div>
      )}
    </PageShell>
  )
}
