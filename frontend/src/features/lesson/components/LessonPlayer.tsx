'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useCallback, useEffect, useState } from 'react'
import { StatsBar, ImagePlate } from '@/shared/ui'
import { EnergyIcon } from '@/shared/ui/icons'
import { GAME_ART } from '@/shared/lib/gameArt'
import {
  ApiError,
  completeLesson,
  getLesson,
  type LearnerStats,
  type LessonSession,
} from '@/shared/lib/api'
import { ExerciseCard } from './ExerciseCard'
import { primaryButtonStyle } from './lessonStyles'
import { EnergyEmpty } from './EnergyEmpty'

export function LessonPlayer({ lessonId }: { lessonId: number }) {
  const router = useRouter()
  const [session, setSession] = useState<LessonSession | null>(null)
  const [stats, setStats] = useState<LearnerStats | null>(null)
  const [index, setIndex] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [outOfEnergy, setOutOfEnergy] = useState<{ message: string; seconds: number } | null>(null)
  const [reward, setReward] = useState<{ energy: number; gems: number } | null>(null)

  // A malformed URL is knowable during render — no effect needed.
  const badLink = !Number.isFinite(lessonId)

  // Bumped to retry the load after a refill, when the first attempt was refused
  // for want of energy.
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (badLink) return
    getLesson(lessonId)
      .then(data => {
        setSession(data)
        setStats(data.stats)
      })
      .catch(err => {
        if (err instanceof ApiError && err.code === 'out_of_energy') {
          setOutOfEnergy({ message: err.message, seconds: err.retryAfterSeconds ?? 0 })
          return
        }
        setError(err instanceof ApiError ? err.message : 'Could not load this lesson.')
      })
  }, [lessonId, badLink, attempt])

  /**
   * Running out mid-lesson leaves the session loaded, so clearing the notice is
   * enough and the learner keeps their place. Running out on arrival left
   * nothing loaded, so that case has to fetch.
   */
  const handleRefilled = useCallback(
    (next: LearnerStats) => {
      setStats(next)
      setOutOfEnergy(null)
      setSession(current => {
        if (!current) setAttempt(a => a + 1)
        return current
      })
    },
    [],
  )

  const exercise = session?.exercises[index]
  const total = session?.exercises.length ?? 0
  // Fills as exercises are cleared, and reads 100% on the reward screen.
  const progress = total > 0 ? (reward ? 1 : index / total) : 0

  const handleOutOfEnergy = useCallback((message: string) => {
    setOutOfEnergy({ message, seconds: 0 })
  }, [])

  async function finish() {
    try {
      const result = await completeLesson(lessonId)
      setStats(result.stats)
      setReward({ energy: result.energyReward, gems: result.gemsReward })
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : 'Could not save this lesson. Your answers are still recorded — try again.',
      )
    }
  }

  function goNext() {
    if (!session) return
    if (index + 1 >= session.exercises.length) {
      void finish()
    } else {
      setIndex(i => i + 1)
    }
  }

  return (
    <div
      className="page-main"
      style={{ minHeight: '100vh', paddingBottom: '2rem' }}
    >
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
          aria-valuenow={reward ? total : index}
          aria-label="Lesson progress"
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

      {session && !reward && !outOfEnergy && (
        <p
          style={{
            margin: '0 0 1rem',
            padding: '0.75rem 1rem',
            borderRadius: 12,
            border: '1px solid rgba(196, 163, 90, 0.35)',
            background: 'rgba(196, 163, 90, 0.08)',
            color: 'var(--muted)',
            fontSize: '0.85rem',
            lineHeight: 1.45,
          }}
        >
          Listen prompts are spoken with a <strong style={{ color: 'var(--text)' }}>Hebrew</strong>{' '}
          system voice as a stand-in. Classical Syriac has no built-in speech engine — this is an
          approximation, not authentic Syriac or the dialect of Jesus.{' '}
          <Link href="/support" style={{ color: 'var(--brand)', fontWeight: 700 }}>
            Device setup
          </Link>
        </p>
      )}

      {(error || badLink) && (
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
          {error ?? 'That lesson link looks wrong.'}
        </div>
      )}

      {outOfEnergy && (
        <EnergyEmpty
          key={outOfEnergy.seconds}
          message={outOfEnergy.message}
          secondsUntilNextEnergy={outOfEnergy.seconds || (stats?.secondsUntilNextEnergy ?? 0)}
          gems={stats?.gems}
          onRefilled={handleRefilled}
        />
      )}

      {reward && (
        <section
          style={{
            textAlign: 'center',
            marginTop: '2.5rem',
            display: 'grid',
            gap: '1.25rem',
            justifyItems: 'center',
          }}
        >
          <div className="brand-font" style={{ fontSize: '2rem', color: 'var(--brand)' }}>
            Lesson complete
          </div>
          <div className="stage-art">
            <ImagePlate
              src={GAME_ART.treasure}
              alt="A messenger with a sealed scroll — lesson finished"
              size="lg"
            />
            <div
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                padding: '0.45rem 0.9rem',
                borderRadius: 999,
                border: '1px solid var(--line)',
                background: 'var(--bg-elevated)',
                color: 'var(--energy)',
                fontWeight: 800,
              }}
            >
              <EnergyIcon size={22} />
              <span>+{reward.energy}</span>
            </div>
          </div>
          <div style={{ color: 'var(--muted)' }}>
            {reward.gems > 0
              ? `+${reward.gems} gems earned`
              : 'Practice run — you already earned the gems for this one'}
          </div>
          <button type="button" onClick={() => router.push('/')} style={primaryButtonStyle}>
            CONTINUE
          </button>
        </section>
      )}

      {!reward && !outOfEnergy && exercise && (
        <ExerciseCard
          key={exercise.id}
          exercise={exercise}
          onStats={setStats}
          onContinue={goNext}
          onOutOfEnergy={handleOutOfEnergy}
        />
      )}
    </div>
  )
}
