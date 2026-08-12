'use client'

import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import Link from 'next/link'
import { PageShell, StatsBar } from '@/shared/ui'
import { ApiError, getPath, type LearningPath } from '@/shared/lib/api'
import { onAccountChanged } from '@/shared/lib/accountEvents'
import { bookmarkFor, summariseUnits } from '../pathModel'
import { Bookmark } from './Bookmark'
import { UnitRow } from './UnitRow'

/** Stable key for a unit's open/closed state, independent of list order. */
function unitKey(sectionNumber: number, unitNumber: number): string {
  return `${sectionNumber}-${unitNumber}`
}

export function LearningPathView() {
  const [path, setPath] = useState<LearningPath | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)
  // Only chapters the learner has opened or closed by hand. Everything else
  // follows the path itself, so their own chapter is open the moment it loads
  // and stays open as progress moves — no effect, and no flash of all-closed.
  const [toggled, setToggled] = useState<Record<string, boolean>>({})

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

  // Signing in or out happens in the app menu now, so the path listens for it
  // rather than owning the control that causes it.
  useEffect(() => onAccountChanged(reload), [reload])

  const summaries = useMemo(() => summariseUnits(path), [path])
  const bookmark = useMemo(() => bookmarkFor(path), [path])

  const toggleUnit = useCallback((key: string, open: boolean) => {
    setToggled(previous => ({ ...previous, [key]: !open }))
  }, [])

  const contentsRef = useRef<HTMLOListElement | null>(null)
  const focusLessonId = bookmark?.lessonId ?? null

  /*
   * Bring the learner's own row into view when the contents page has grown past
   * a screen. Deliberately conditional: with a short curriculum their chapter is
   * already visible and nothing moves, which keeps the bookmark — the primary
   * way back in — on screen. It only scrolls once their place is genuinely below
   * the fold, and only on load, so opening a chapter by hand never yanks the page.
   *
   * Measured synchronously rather than inside requestAnimationFrame: rAF does not
   * fire in a tab that is not compositing (backgrounded, or restored in the
   * background), which would silently skip the scroll. Nothing here needs a frame
   * — the rows hold no images, and the bookmark's art carries explicit dimensions,
   * so layout is already settled by the time effects run.
   */
  useEffect(() => {
    const contents = contentsRef.current
    if (!contents || focusLessonId === null) return

    const target = contents.querySelector(`[data-lesson-id="${focusLessonId}"]`)
    if (!target) return // their chapter is collapsed — nothing to scroll to

    const box = target.getBoundingClientRect()
    if (box.top >= 0 && box.bottom <= window.innerHeight) return

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    target.scrollIntoView({ block: 'center', behavior: reduceMotion ? 'auto' : 'smooth' })
  }, [focusLessonId, reloadKey])

  return (
    <PageShell>
      <header className="path-header">
        <div>
          <div className="brand-font page-title">AramiGo</div>
          <div style={{ color: 'var(--muted)', fontSize: '0.95rem' }}>
            Classical Syriac · beginners
          </div>
          <div
            style={{
              color: 'var(--muted)',
              fontSize: '0.8rem',
              marginTop: '0.35rem',
              lineHeight: 1.45,
              maxWidth: '36rem',
            }}
          >
            Listen mode uses a Hebrew system voice as a stand-in (not authentic Syriac audio).{' '}
            <Link href="/support" style={{ color: 'var(--brand)', fontWeight: 700 }}>
              Device setup
            </Link>
          </div>
        </div>
        {path && <StatsBar stats={path.stats} />}
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

      {bookmark && <Bookmark bookmark={bookmark} />}

      {summaries.length > 0 && (
        <ol className="contents-list" aria-label="Course contents" ref={contentsRef}>
          {summaries.map(summary => {
            const key = unitKey(summary.unit.sectionNumber, summary.unit.unitNumber)
            const open = toggled[key] ?? summary.active
            return (
              <UnitRow
                key={key}
                summary={summary}
                open={open}
                onToggle={() => toggleUnit(key, open)}
              />
            )
          })}
        </ol>
      )}

      {!path && !error && (
        <p style={{ color: 'var(--muted)', textAlign: 'center', marginTop: '3rem' }}>
          Loading your path…
        </p>
      )}
    </PageShell>
  )
}
