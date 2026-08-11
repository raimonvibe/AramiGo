import Link from 'next/link'
import type { NodeKind, PathNode } from '@/shared/lib/api'

const KIND_ICON: Record<NodeKind, string> = {
  STAR: '✦',
  CHEST: '❖',
  CHARACTER: '☙',
}

const KIND_LABEL: Record<NodeKind, string> = {
  STAR: 'Lesson',
  CHEST: 'Treasure',
  CHARACTER: 'Practice',
}

const NODE_SIZE = 84

function nodeSurface(status: PathNode['status']) {
  switch (status) {
    case 'CURRENT':
      return {
        background: 'linear-gradient(160deg, var(--accent), var(--accent-deep))',
        border: '3px solid var(--brand)',
        color: '#07130f',
        shadow: '0 0 0 6px rgba(63, 159, 132, 0.16), var(--shadow)',
      }
    case 'COMPLETED':
      return {
        background: 'linear-gradient(160deg, var(--brand), var(--brand-deep))',
        border: '3px solid rgba(196, 163, 90, 0.55)',
        color: '#1b1406',
        shadow: 'var(--shadow)',
      }
    case 'LOCKED':
      return {
        background: 'var(--bg-elevated)',
        border: '3px solid var(--line)',
        color: 'var(--muted)',
        shadow: 'none',
      }
  }
}

/**
 * A single stop on the path. The serpentine offset comes from the index so the
 * column reads as a route through the manuscript rather than a list of rows.
 */
export function LessonNode({ node, offset }: { node: PathNode; offset: number }) {
  const surface = nodeSurface(node.status)
  const playable = node.status !== 'LOCKED'
  const kindLabel = KIND_LABEL[node.nodeKind]
  const partly = node.solvedCount > 0 && node.solvedCount < node.exerciseCount

  const circle = (
    <div
      style={{
        width: NODE_SIZE,
        height: NODE_SIZE,
        borderRadius: '50%',
        display: 'grid',
        placeItems: 'center',
        background: surface.background,
        border: surface.border,
        color: surface.color,
        boxShadow: surface.shadow,
        fontSize: '2rem',
        transition: 'transform 160ms ease',
      }}
      aria-hidden="true"
    >
      {node.status === 'LOCKED' ? '⌧' : KIND_ICON[node.nodeKind]}
    </div>
  )

  const caption = (
    <div style={{ display: 'grid', gap: '0.15rem', justifyItems: 'center', maxWidth: 150 }}>
      <span
        className="brand-font"
        style={{
          fontSize: '0.98rem',
          fontWeight: 700,
          color: node.status === 'LOCKED' ? 'var(--muted)' : 'var(--text)',
          textAlign: 'center',
        }}
      >
        {node.title}
      </span>
      <span
        style={{
          fontSize: '0.7rem',
          fontWeight: 700,
          letterSpacing: '0.08em',
          textTransform: 'uppercase',
          color: 'var(--muted)',
        }}
      >
        {node.status === 'CURRENT' ? 'Start' : kindLabel}
        {partly && ` · ${node.solvedCount}/${node.exerciseCount}`}
      </span>
    </div>
  )

  const body = (
    <div
      style={{
        display: 'grid',
        gap: '0.5rem',
        justifyItems: 'center',
        transform: `translateX(${offset}px)`,
      }}
    >
      {circle}
      {caption}
    </div>
  )

  if (!playable) {
    return (
      <div
        aria-label={`Lesson ${node.position}: ${node.title} — locked`}
        aria-disabled="true"
        style={{ opacity: 0.55 }}
      >
        {body}
      </div>
    )
  }

  return (
    <Link
      href={`/lesson/${node.lessonId}`}
      aria-label={`Lesson ${node.position}: ${node.title} — ${
        node.status === 'CURRENT' ? 'start' : 'practice again'
      }`}
      style={{ textDecoration: 'none', color: 'inherit' }}
    >
      {body}
    </Link>
  )
}
