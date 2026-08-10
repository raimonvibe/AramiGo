'use client'

import { useEffect, useState } from 'react'
import { BottomNav, StatsBar } from '@/shared/ui'
import { getPath, type LearningPath } from '@/shared/lib/api'
import { LessonChapterCard } from './LessonChapterCard'

export function LearningPathView() {
  const [path, setPath] = useState<LearningPath | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getPath()
      .then(setPath)
      .catch(err => setError(err instanceof Error ? err.message : 'Could not load path'))
  }, [])

  return (
    <div style={{ minHeight: '100vh', display: 'grid', gridTemplateRows: '1fr auto' }}>
      <main style={{ width: 'min(480px, 100%)', margin: '0 auto', padding: '1.25rem 1rem 2rem' }}>
        <header
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: '1rem',
            marginBottom: '1.25rem',
          }}
        >
          <div>
            <div className="brand-font" style={{ fontSize: '1.7rem', color: 'var(--brand)' }}>
              AramiGo
            </div>
            <div style={{ color: 'var(--muted)', fontSize: '0.9rem' }}>Classical Syriac · beginners</div>
          </div>
          {path && <StatsBar stats={path.stats} />}
        </header>

        {error && (
          <div
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
            <div style={{ marginTop: '0.5rem', color: 'var(--muted)', fontSize: '0.9rem' }}>
              Is the Spring Boot API running on port 8080?
            </div>
          </div>
        )}

        {path && (
          <>
            <section
              style={{
                background: 'linear-gradient(135deg, #3f9f84, #2a6f5c 55%, #8f7435)',
                borderRadius: '22px',
                padding: '1.15rem 1.25rem',
                boxShadow: 'var(--shadow)',
                marginBottom: '1.5rem',
              }}
            >
              <div style={{ opacity: 0.9, fontWeight: 800, letterSpacing: '0.06em', fontSize: '0.8rem' }}>
                SECTION {path.sectionNumber}, UNIT {path.unitNumber}
              </div>
              <h1 className="brand-font" style={{ margin: '0.35rem 0 0.25rem', fontSize: '1.65rem' }}>
                {path.title}
              </h1>
              <p style={{ margin: 0, opacity: 0.92 }}>{path.description}</p>
            </section>

            <section
              aria-label="Lesson chapters"
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '0.75rem',
                padding: '0.25rem 0 1.5rem',
              }}
            >
              <div
                style={{
                  fontSize: '0.75rem',
                  fontWeight: 800,
                  letterSpacing: '0.1em',
                  textTransform: 'uppercase',
                  color: 'var(--muted)',
                  marginBottom: '0.15rem',
                }}
              >
                Chapters
              </div>
              {path.nodes.map(node => (
                <LessonChapterCard key={node.lessonId} node={node} />
              ))}
            </section>
          </>
        )}

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
