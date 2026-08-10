import Link from 'next/link'
import type { PathNode } from '@/shared/lib/api'

function nodeLabel(kind: string) {
  if (kind === 'CHEST') return '▣'
  if (kind === 'CHARACTER') return '☺'
  return '★'
}

export function PathNodeButton({ node }: { node: PathNode }) {
  const playable = node.status === 'CURRENT' || node.status === 'COMPLETED'
  const locked = node.status === 'LOCKED'
  const current = node.status === 'CURRENT'

  const circle = (
    <div
      style={{
        width: 78,
        height: 78,
        borderRadius: '50%',
        display: 'grid',
        placeItems: 'center',
        fontSize: '1.7rem',
        fontWeight: 700,
        background: locked
          ? '#2a343c'
          : current
            ? 'linear-gradient(160deg, var(--accent), var(--accent-deep))'
            : 'linear-gradient(160deg, var(--brand), var(--brand-deep))',
        color: locked ? '#6a7882' : '#0f161a',
        boxShadow: current ? '0 0 0 6px rgba(63, 159, 132, 0.25)' : 'var(--shadow)',
        opacity: locked ? 0.75 : 1,
        transform: current ? 'scale(1.05)' : 'none',
        transition: 'transform 180ms ease, box-shadow 180ms ease',
      }}
    >
      {nodeLabel(node.nodeKind)}
    </div>
  )

  return (
    <div style={{ display: 'grid', justifyItems: 'center', gap: '0.55rem' }}>
      {current && (
        <div
          style={{
            background: '#0b1014',
            color: 'var(--accent)',
            fontWeight: 800,
            letterSpacing: '0.08em',
            fontSize: '0.75rem',
            padding: '0.35rem 0.7rem',
            borderRadius: '10px',
            border: '1px solid var(--line)',
          }}
        >
          START
        </div>
      )}
      {playable ? (
        <Link href={`/lesson/${node.lessonId}`} aria-label={node.title}>
          {circle}
        </Link>
      ) : (
        <div aria-disabled="true">{circle}</div>
      )}
      <div style={{ color: 'var(--muted)', fontSize: '0.85rem', fontWeight: 600 }}>{node.title}</div>
    </div>
  )
}
