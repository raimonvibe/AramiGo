'use client'

import Link from 'next/link'
import { useCallback, useEffect, useState } from 'react'
import { StatsBar } from '@/shared/ui'
import { ApiError, getReview, type LearnerStats, type ReviewSession } from '@/shared/lib/api'
import { ExerciseCard, primaryButtonStyle } from '@/features/lesson'
import { EnergyEmpty } from '@/features/lesson'

/**
 * Plays exercises that have come due again.
 *
 * <p>Deliberately not the lesson player. A review has nothing to complete, so
 * there is no reward screen, no energy or gems at the end, and no POST when the
 * last card is cleared — the schedule has already moved with each answer. What
 * it does have is a count of everything still owed, because the session is
 * capped and the learner should know there is more behind it.
 */
export function ReviewPlayer() {
  const [session, setSession] = useState<ReviewSession | null>(null)
  const [stats, setStats] = useState<LearnerStats | null>(null)
  const [index, setIndex] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [outOfEnergy, setOutOfEnergy] = useState<{ message: string; seconds: number } | null>(null)

  useEffect(() => {
    getReview()
      .then(data => {
        setSession(data)
        setStats(data.stats)
      })
      .catch(err => {
        if (err instanceof ApiError && err.code === 'out_of_energy') {
          setOutOfEnergy({ message: err.message, seconds: err.retryAfterSeconds ?? 0 })
          return
        }
        setError(err instanceof ApiError ? err.message : 'Could not load your review.')
      })
  }, [])

  const handleOutOfEnergy = useCallback((message: string) => {
    setOutOfEnergy({ message, seconds: 0 })
  }, [])

  const total = session?.exercises.length ?? 0
  const exercise = session?.exercises[index]
  const finished = session !== null && index >= total
  const progress = total > 0 ? index / total : 0

  // Everything handed out, minus what is now cleared.
  const stillOwed = session ? Math.max(0, session.dueCount - index) : 0

  return (
    <div className="page-main" style={{ minHeight: '100vh', paddingBottom: '2rem' }}>
      <header style={{ display: 'grid', gap: '0.75rem', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Link href="/" style={{ fontSize: '1.4rem', color: 'var(--muted)' }} aria-label="Close">
            ✕
          </Link>
          {stats && <StatsBar stats={stats} />}
        </div>
        <div
          role="progressbar"
          aria-valuemin={0}
          aria-valuemax={total}
          aria-valuenow={index}
          aria-label="Review progress"
          style={{ height: 14, borderRadius: 999, background: '#2a343c', overflow: 'hidden' }}
        >
          <div
            style={{
              width: `${Math.min(100, progress * 100)}%`,
              height: '100%',
              background: 'linear-gradient(90deg, var(--brand), var(--accent))',
              transition: 'width 240ms ease',
            }}
          />
        </div>
      </header>

      {error && (
        <div
          role="alert"
          style={{
            background: 'rgba(224, 122, 95, 0.12)',
            border: '1px solid var(--danger)',
            color: 'var(--danger)',
            padding: '1rem',
            borderRadius: 'var(--radius)',
            marginBottom: '1rem',
          }}
        >
          {error}
        </div>
      )}

      {outOfEnergy && (
        <EnergyEmpty
          key={outOfEnergy.seconds}
          message={outOfEnergy.message}
          secondsUntilNextEnergy={outOfEnergy.seconds || (stats?.secondsUntilNextEnergy ?? 0)}
        />
      )}

      {!error && !outOfEnergy && session && total === 0 && (
        <div style={{ display: 'grid', gap: '1rem', justifyItems: 'start' }}>
          <h1 style={{ margin: 0 }} className="brand-font">
            Nothing to review yet
          </h1>
          <p style={{ margin: 0, color: 'var(--muted)', lineHeight: 1.6 }}>
            Words come back here a day after you first get them right, then at wider and wider
            gaps. Finish a lesson and check back tomorrow.
          </p>
          <Link href="/" style={primaryButtonStyle}>
            BACK TO THE PATH
          </Link>
        </div>
      )}

      {!error && !outOfEnergy && exercise && (
        <ExerciseCard
          key={exercise.id}
          exercise={exercise}
          onContinue={() => setIndex(i => i + 1)}
          onStats={setStats}
          onOutOfEnergy={handleOutOfEnergy}
        />
      )}

      {!error && !outOfEnergy && finished && total > 0 && (
        <div style={{ display: 'grid', gap: '1rem', justifyItems: 'start' }}>
          <h1 style={{ margin: 0 }} className="brand-font">
            Review done
          </h1>
          <p style={{ margin: 0, color: 'var(--muted)', lineHeight: 1.6 }}>
            {stillOwed > 0
              ? `${stillOwed} more ${stillOwed === 1 ? 'word is' : 'words are'} still waiting. They will keep until you want them.`
              : 'Everything due is cleared. Each of these will come back later, further apart than last time.'}
          </p>
          {stillOwed > 0 && (
            <button type="button" onClick={() => window.location.reload()} style={primaryButtonStyle}>
              KEEP GOING
            </button>
          )}
          <Link href="/" style={{ color: 'var(--brand)', fontWeight: 700 }}>
            Back to the path
          </Link>
        </div>
      )}
    </div>
  )
}
