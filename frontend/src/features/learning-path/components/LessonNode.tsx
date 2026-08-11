import Link from 'next/link'
import type { NodeKind, PathNode } from '@/shared/lib/api'

const KIND_LABEL: Record<NodeKind, string> = {
  STAR: 'Lesson',
  CHEST: 'Treasure',
  CHARACTER: 'Practice',
}

function sealColors(status: PathNode['status']) {
  switch (status) {
    case 'CURRENT':
      return {
        bg: 'linear-gradient(160deg, var(--accent), var(--accent-deep))',
        border: '2px solid var(--brand)',
        color: '#07130f',
        ring: '0 0 0 4px rgba(63, 159, 132, 0.18)',
      }
    case 'COMPLETED':
      return {
        bg: 'linear-gradient(160deg, var(--brand), var(--brand-deep))',
        border: '2px solid rgba(196, 163, 90, 0.5)',
        color: '#1b1406',
        ring: 'none',
      }
    case 'LOCKED':
      return {
        bg: 'var(--bg-elevated)',
        border: '2px solid var(--line)',
        color: 'var(--muted)',
        ring: 'none',
      }
  }
}

/**
 * Manuscript chapter row: numbered Literata seal + title + kind in words.
 * No pictograms, no serpentine offset — the unit rail carries progress.
 */
export function LessonNode({ node }: { node: PathNode }) {
  const seal = sealColors(node.status)
  const playable = node.status !== 'LOCKED'
  const kindLabel = KIND_LABEL[node.nodeKind]
  const partly = node.solvedCount > 0 && node.solvedCount < node.exerciseCount

  const body = (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: '56px 1fr',
        gap: '1rem',
        alignItems: 'center',
        opacity: node.status === 'LOCKED' ? 0.58 : 1,
      }}
    >
      <div
        className="brand-font"
        aria-hidden="true"
        style={{
          width: 56,
          height: 56,
          borderRadius: '50%',
          display: 'grid',
          placeItems: 'center',
          background: seal.bg,
          border: seal.border,
          boxShadow: seal.ring,
          color: seal.color,
          fontSize: '1.45rem',
          fontWeight: 700,
          lineHeight: 1,
          zIndex: 1,
        }}
      >
        {node.position}
      </div>

      <div style={{ display: 'grid', gap: '0.2rem', minWidth: 0 }}>
        <span
          className="brand-font"
          style={{
            fontSize: '1.2rem',
            color: node.status === 'LOCKED' ? 'var(--muted)' : 'var(--text)',
            lineHeight: 1.25,
          }}
        >
          {node.title}
        </span>
        <span
          style={{
            fontSize: '0.75rem',
            fontWeight: 700,
            letterSpacing: '0.06em',
            textTransform: 'uppercase',
            color:
              node.status === 'CURRENT'
                ? 'var(--accent)'
                : node.status === 'COMPLETED'
                  ? 'var(--brand)'
                  : 'var(--muted)',
          }}
        >
          {node.status === 'CURRENT' ? 'Current · ' : ''}
          {kindLabel}
          {partly ? ` · ${node.solvedCount}/${node.exerciseCount}` : ''}
          {node.status === 'COMPLETED' && !partly ? ' · Done' : ''}
          {node.status === 'LOCKED' ? ' · Locked' : ''}
        </span>
      </div>
    </div>
  )

  if (!playable) {
    return (
      <div aria-label={`Lesson ${node.position}: ${node.title} — locked`} aria-disabled="true">
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
      style={{ textDecoration: 'none', color: 'inherit', display: 'block' }}
    >
      {body}
    </Link>
  )
}
