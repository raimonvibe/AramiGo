'use client'

import { useCallback, useEffect, useState } from 'react'
import { BottomNav, StatsBar } from '@/shared/ui'
import { AccountBar } from '@/features/auth'
import { ApiError, getPath, type LearningPath } from '@/shared/lib/api'
import { LessonNode } from './LessonNode'

/** Serpentine offsets, repeating every six nodes. */
const OFFSETS = [0, 48, 72, 48, 0, -48, -72, -48]

export function LearningPathView() {
  const [path, setPath] = useState<LearningPath | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    getPath()
      .then(data => {
        if (cancelled) return
        setPath(data)
        setError(null)
      })
      .catch(err => {
        if (cancelled) return
        setError(err instanceof ApiError ? err.message : 'Could not load your learning path.')
      })
    return () => {
      cancelled = true
    }
  }, [reloadKey])

  const reload = useCallback(() => setReloadKey(key => key + 1), [])

  return (
    <div style={{ minHeight: '100vh', display: 'grid', gridTemplateRows: '1fr auto' }}>
      <main style={{ width: 'min(480px, 100%)', margin: '0 auto', padding: '1.25rem 1rem 2rem' }}>
        <header
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: '1rem',
            marginBottom: '1rem',
          }}
        >
          <div>
            <div className="brand-font" style={{ fontSize: '1.7rem', color: 'var(--brand)' }}>
              AramiGo
            </div>
            <div style={{ color: 'var(--muted)', fontSize: '0.9rem' }}>
              Classical Syriac · beginners
            </div>
          </div>
          {path && <StatsBar stats={path.stats} />}
        </header>

        <AccountBar onAccountChanged={reload} />

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
              display: 'grid',
              gap: '0.75rem',
              justifyItems: 'start',
            }}
          >
            <span>{error}</span>
            <button
              type="button"
              onClick={reload}
              style={{
                border: '1px solid var(--danger)',
                background: 'transparent',
                color: 'var(--danger)',
                borderRadius: 12,
                padding: '0.4rem 0.9rem',
                fontWeight: 700,
              }}
            >
              Try again
            </button>
          </div>
        )}

        {path?.units.map(unit => (
          <section key={`${unit.sectionNumber}-${unit.unitNumber}`} style={{ marginBottom: '2rem' }}>
            <div
              style={{
                background: 'linear-gradient(135deg, var(--unit-from), var(--unit-to))',
                borderRadius: '22px',
                padding: '1.15rem 1.25rem',
                boxShadow: 'var(--shadow)',
                marginBottom: '1.75rem',
              }}
            >
              <div
                style={{
                  opacity: 0.9,
                  fontWeight: 800,
                  letterSpacing: '0.06em',
                  fontSize: '0.8rem',
                }}
              >
                SECTION {unit.sectionNumber}, UNIT {unit.unitNumber}
              </div>
              <h1
                className="brand-font"
                style={{ margin: '0.35rem 0 0.25rem', fontSize: '1.65rem' }}
              >
                {unit.title}
              </h1>
              <p style={{ margin: 0, opacity: 0.92 }}>{unit.description}</p>
            </div>

            <ol
              aria-label={`Lessons in unit ${unit.unitNumber}`}
              style={{
                listStyle: 'none',
                margin: 0,
                padding: 0,
                display: 'grid',
                gap: '1.5rem',
                justifyItems: 'center',
              }}
            >
              {unit.nodes.map((node, index) => (
                <li key={node.lessonId}>
                  <LessonNode node={node} offset={OFFSETS[index % OFFSETS.length]} />
                </li>
              ))}
            </ol>
          </section>
        ))}

        {!path && !error && (
          <p style={{ color: 'var(--muted)', textAlign: 'center', marginTop: '3rem' }}>
            Loading your path…
          </p>
        )}
      </main>
      <BottomNav />
    </div>
  )
}
