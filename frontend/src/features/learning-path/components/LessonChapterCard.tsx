import Link from 'next/link'
import type { PathNode } from '@/shared/lib/api'

function kindLabel(kind: string): string | null {
  if (kind === 'CHEST') return 'Treasure'
  if (kind === 'CHARACTER') return 'Practice'
  return null
}

function statusSeal(status: PathNode['status']): { label: string; color: string; bg: string } {
  switch (status) {
    case 'CURRENT':
      return { label: 'Current', color: 'var(--accent)', bg: 'rgba(63, 159, 132, 0.18)' }
    case 'COMPLETED':
      return { label: 'Done', color: 'var(--brand)', bg: 'rgba(196, 163, 90, 0.16)' }
    case 'LOCKED':
      return { label: 'Locked', color: 'var(--muted)', bg: 'rgba(154, 171, 182, 0.12)' }
  }
}

export function LessonChapterCard({ node }: { node: PathNode }) {
  const playable = node.status === 'CURRENT' || node.status === 'COMPLETED'
  const locked = node.status === 'LOCKED'
  const current = node.status === 'CURRENT'
  const completed = node.status === 'COMPLETED'
  const seal = statusSeal(node.status)
  const kind = kindLabel(node.nodeKind)

  const card = (
    <article
      style={{
        display: 'grid',
        gridTemplateColumns: 'auto 1fr auto',
        gap: '0.85rem',
        alignItems: 'center',
        width: '100%',
        padding: '1rem 1.1rem',
        borderRadius: 16,
        border: current
          ? '1.5px solid var(--accent)'
          : completed
            ? '1px solid rgba(196, 163, 90, 0.35)'
            : '1px solid var(--line)',
        background: current
          ? 'linear-gradient(135deg, rgba(63, 159, 132, 0.14), var(--bg-elevated))'
          : completed
            ? 'rgba(26, 36, 44, 0.72)'
            : 'var(--bg-elevated)',
        opacity: locked ? 0.62 : 1,
        boxShadow: current ? '0 0 0 3px rgba(63, 159, 132, 0.12)' : 'none',
        transition: 'border-color 160ms ease, box-shadow 160ms ease, opacity 160ms ease',
        cursor: playable ? 'pointer' : 'default',
      }}
    >
      {/* Manuscript margin rule + lesson index */}
      <div
        style={{
          display: 'grid',
          gap: '0.35rem',
          justifyItems: 'center',
          minWidth: '3.25rem',
          borderLeft: '2px solid var(--brand-deep)',
          paddingLeft: '0.65rem',
        }}
      >
        <span
          className="brand-font"
          style={{
            fontSize: '1.35rem',
            fontWeight: 700,
            color: current ? 'var(--accent)' : locked ? 'var(--muted)' : 'var(--brand)',
            lineHeight: 1,
          }}
        >
          {node.position}
        </span>
        <span
          style={{
            fontSize: '0.65rem',
            fontWeight: 700,
            letterSpacing: '0.06em',
            textTransform: 'uppercase',
            color: 'var(--muted)',
          }}
        >
          Lesson
        </span>
      </div>

      <div style={{ minWidth: 0 }}>
        <h2
          className="brand-font"
          style={{
            margin: 0,
            fontSize: '1.15rem',
            fontWeight: 700,
            color: locked ? 'var(--muted)' : 'var(--text)',
          }}
        >
          {node.title}
        </h2>
        {kind && (
          <p
            style={{
              margin: '0.3rem 0 0',
              fontSize: '0.8rem',
              fontWeight: 600,
              color: 'var(--muted)',
              letterSpacing: '0.02em',
            }}
          >
            {kind}
          </p>
        )}
      </div>

      <span
        style={{
          fontSize: '0.7rem',
          fontWeight: 800,
          letterSpacing: '0.07em',
          textTransform: 'uppercase',
          color: seal.color,
          background: seal.bg,
          border: `1px solid ${seal.color}`,
          borderRadius: 999,
          padding: '0.35rem 0.65rem',
          whiteSpace: 'nowrap',
        }}
      >
        {seal.label}
      </span>
    </article>
  )

  if (playable) {
    return (
      <Link
        href={`/lesson/${node.lessonId}`}
        aria-label={`Lesson ${node.position}: ${node.title}`}
        style={{ display: 'block', width: '100%', textDecoration: 'none', color: 'inherit' }}
      >
        {card}
      </Link>
    )
  }

  return (
    <div aria-disabled="true" aria-label={`Lesson ${node.position}: ${node.title} (locked)`}>
      {card}
    </div>
  )
}
