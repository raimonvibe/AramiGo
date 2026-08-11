'use client'

import Script from 'next/script'
import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react'
import { BottomNav, SiteFooter } from '@/shared/ui'
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
      // Google-hosted avatar; alt text is the learner's name when known.
      <img
        src={pictureUrl}
        alt={displayName ? `${displayName}'s profile photo` : 'Google profile photo'}
        width={88}
        height={88}
        referrerPolicy="no-referrer"
        style={{
          width: 88,
          height: 88,
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
        width: 88,
        height: 88,
        borderRadius: '50%',
        display: 'grid',
        placeItems: 'center',
        flexShrink: 0,
        border: '2px solid var(--brand)',
        background: 'linear-gradient(135deg, var(--unit-from), var(--unit-to))',
        color: 'var(--text)',
        fontFamily: 'var(--font-brand), Georgia, serif',
        fontSize: '2rem',
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
        gap: '0.2rem',
        padding: '0.85rem 0',
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
        <span style={{ color: 'var(--muted)', fontSize: '0.88rem', fontWeight: 700 }}>{label}</span>
        <span
          style={{
            color,
            fontWeight: 800,
            fontSize: '1.05rem',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.35rem',
          }}
        >
          {icon}
          {value}
        </span>
      </div>
      <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.82rem', lineHeight: 1.45 }}>{hint}</p>
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
      width: 280,
    })
  }, [scriptReady, profile?.signedIn, onCredential])

  function signOut() {
    googleIdentity()?.disableAutoSelect()
    clearToken()
    setStatus(null)
    void refresh()
  }

  const percent =
    profile == null ? 0 : progressPercent(profile.completedLessons, profile.totalLessons)
  const signedIn = profile?.signedIn === true

  return (
    <div style={{ minHeight: '100vh', display: 'grid', gridTemplateRows: '1fr auto' }}>
      {GOOGLE_SIGN_IN_ENABLED && <Script src={GSI_SCRIPT_SRC} onReady={() => setScriptReady(true)} />}

      <main
        style={{
          width: 'min(560px, 100%)',
          margin: '0 auto',
          padding: '1.25rem 1rem 2rem',
        }}
      >
        <header style={{ marginBottom: '1.5rem' }}>
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
          <h1
            className="brand-font"
            style={{ fontSize: 'clamp(1.6rem, 5vw, 2rem)', color: 'var(--brand)', margin: '0.25rem 0 0' }}
          >
            Account
          </h1>
        </header>

        {error && (
          <p role="alert" style={{ color: 'var(--danger)', marginTop: 0 }}>
            {error}
          </p>
        )}

        {!profile && !error && (
          <p style={{ color: 'var(--muted)' }} role="status">
            Loading your account…
          </p>
        )}

        {profile && (
          <div style={{ display: 'grid', gap: '1.75rem' }}>
            <section
              aria-label="Profile"
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                alignItems: 'center',
                gap: '1.1rem',
                padding: '1.1rem 1rem',
                borderRadius: 16,
                border: '1px solid var(--line)',
                background: 'var(--bg-elevated)',
              }}
            >
              <ProfileAvatar pictureUrl={profile.pictureUrl} displayName={profile.displayName} />
              <div style={{ flex: '1 1 12rem', minWidth: 0 }}>
                <p
                  className="brand-font"
                  style={{
                    margin: 0,
                    fontSize: '1.35rem',
                    color: 'var(--text)',
                    overflowWrap: 'anywhere',
                  }}
                >
                  {signedIn
                    ? profile.displayName || 'Signed in'
                    : 'Learning as a guest'}
                </p>
                {signedIn && profile.email ? (
                  <p
                    style={{
                      margin: '0.35rem 0 0',
                      color: 'var(--muted)',
                      fontSize: '0.95rem',
                      overflowWrap: 'anywhere',
                    }}
                  >
                    {profile.email}
                  </p>
                ) : (
                  <p style={{ margin: '0.35rem 0 0', color: 'var(--muted)', fontSize: '0.9rem', lineHeight: 1.45 }}>
                    Sign in with Google to save progress across devices and show your email here.
                  </p>
                )}
                {signedIn && (
                  <button
                    type="button"
                    onClick={signOut}
                    style={{
                      marginTop: '0.85rem',
                      border: '1px solid var(--line)',
                      background: 'transparent',
                      color: 'var(--muted)',
                      borderRadius: 999,
                      padding: '0.45rem 0.95rem',
                      fontWeight: 700,
                      fontSize: '0.85rem',
                    }}
                  >
                    Sign out
                  </button>
                )}
              </div>
            </section>

            {!signedIn && GOOGLE_SIGN_IN_ENABLED && (
              <section aria-label="Sign in" style={{ display: 'grid', gap: '0.65rem' }}>
                <h2
                  className="brand-font"
                  style={{ margin: 0, fontSize: '1.15rem', color: 'var(--text)' }}
                >
                  Keep learning on any device
                </h2>
                <p style={{ margin: 0, color: 'var(--muted)', lineHeight: 1.55, fontSize: '0.95rem' }}>
                  Everything you have already done as a guest comes with you. No lesson is lost when
                  you sign in.
                </p>
                <div ref={buttonHost} style={{ marginTop: '0.35rem' }} />
              </section>
            )}

            {!GOOGLE_SIGN_IN_ENABLED && !signedIn && (
              <p style={{ margin: 0, color: 'var(--muted)', lineHeight: 1.55 }}>
                Google sign-in is not configured in this environment. You can still learn as a
                guest on this device.
              </p>
            )}

            {status && (
              <p role="status" style={{ margin: 0, color: 'var(--muted)' }}>
                {status}
              </p>
            )}

            <section aria-label="Lesson progress" style={{ display: 'grid', gap: '0.75rem' }}>
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'baseline',
                  gap: '0.75rem',
                  flexWrap: 'wrap',
                }}
              >
                <h2
                  className="brand-font"
                  style={{ margin: 0, fontSize: '1.15rem', color: 'var(--text)' }}
                >
                  Path progress
                </h2>
                <span style={{ color: 'var(--brand)', fontWeight: 800 }}>
                  {profile.completedLessons} / {profile.totalLessons} lessons
                </span>
              </div>
              <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.92rem', lineHeight: 1.5 }}>
                {profile.completedLessons === 0
                  ? 'You have not finished a lesson yet — open the path and start with the first seal.'
                  : profile.completedLessons >= profile.totalLessons && profile.totalLessons > 0
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
                  height: 10,
                  borderRadius: 999,
                  background: 'var(--bg-chip)',
                  border: '1px solid var(--line)',
                  overflow: 'hidden',
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

            <section aria-label="Rewards and energy" style={{ display: 'grid', gap: '0.15rem' }}>
              <h2
                className="brand-font"
                style={{ margin: '0 0 0.35rem', fontSize: '1.15rem', color: 'var(--text)' }}
              >
                Rewards
              </h2>
              <StatRow
                label="Gems"
                value={String(profile.stats.gems)}
                icon={<GemIcon size={18} />}
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
                icon={<StreakIcon size={18} />}
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
                icon={<EnergyIcon size={18} />}
                hint={energyHint(profile.stats)}
                color="var(--energy)"
              />
            </section>
          </div>
        )}
      </main>

      <SiteFooter />
      <BottomNav />
    </div>
  )
}
