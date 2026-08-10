'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { StatsBar } from '@/shared/ui'
import {
  completeLesson,
  getLesson,
  type LearnerStats,
  type LessonSession,
} from '@/shared/lib/api'
import { ExerciseCard, primaryButtonStyle } from './ExerciseCard'

export function LessonPlayer({ lessonId }: { lessonId: number }) {
  const router = useRouter()
  const [session, setSession] = useState<LessonSession | null>(null)
  const [stats, setStats] = useState<LearnerStats | null>(null)
  const [index, setIndex] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [reward, setReward] = useState<{ energy: number; gems: number } | null>(null)

  useEffect(() => {
    if (!Number.isFinite(lessonId)) return
    getLesson(lessonId)
      .then(data => {
        setSession(data)
        setStats(data.stats)
      })
      .catch(err => setError(err instanceof Error ? err.message : 'Could not load lesson'))
  }, [lessonId])

  const exercise = session?.exercises[index]
  const progress =
    session && session.exercises.length > 0
      ? (index + (reward ? 1 : 0)) / session.exercises.length
      : 0

  async function finish() {
    const result = await completeLesson(lessonId)
    setStats(result.stats)
    setReward({ energy: result.energyReward, gems: result.gemsReward })
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
    <div style={{ minHeight: '100vh', width: 'min(520px, 100%)', margin: '0 auto', padding: '1rem' }}>
      <header style={{ display: 'grid', gap: '0.75rem', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Link href="/" style={{ fontSize: '1.4rem', color: 'var(--muted)' }} aria-label="Close">
            ✕
          </Link>
          {stats && <StatsBar stats={stats} />}
        </div>
        <div style={{ height: 14, borderRadius: 999, background: '#2a343c', overflow: 'hidden' }}>
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

      {error && <div style={{ color: 'var(--danger)', whiteSpace: 'pre-wrap' }}>{error}</div>}

      {reward && (
        <section
          style={{
            textAlign: 'center',
            marginTop: '3rem',
            display: 'grid',
            gap: '1.25rem',
            justifyItems: 'center',
          }}
        >
          <div className="brand-font" style={{ fontSize: '2rem', color: 'var(--brand)' }}>
            Lesson complete
          </div>
          <div
            style={{
              width: 140,
              height: 140,
              borderRadius: 28,
              background: 'linear-gradient(160deg, var(--energy), #8f4d66)',
              display: 'grid',
              placeItems: 'center',
              fontSize: '2rem',
              fontWeight: 800,
              boxShadow: 'var(--shadow)',
            }}
          >
            +{reward.energy} ⚡
          </div>
          <div style={{ color: 'var(--muted)' }}>+{reward.gems} gems earned</div>
          <button type="button" onClick={() => router.push('/')} style={primaryButtonStyle}>
            CONTINUE
          </button>
        </section>
      )}

      {!reward && exercise && (
        <ExerciseCard
          key={exercise.id}
          exercise={exercise}
          onStats={setStats}
          onContinue={goNext}
        />
      )}
    </div>
  )
}
