import Link from 'next/link'
import type { NodeKind, PathNode } from '@/shared/lib/api'

/**
 * One stop on the path, drawn as a numbered seal on a ruled manuscript column.
 *
 * The seal carries the lesson number rather than a game icon, and the kind is
 * named in words underneath. Chapter roundels running down a ruled margin are a
 * manuscript idiom; a winding trail of pictogram buttons is someone else's.
 */

const KIND_LABEL: Record<NodeKind, string> = {
  STAR: 'Lesson',
  CHEST: 'Treasure',
  CHARACTER: 'Practice',
}

function statusNote(node: PathNode): string {
  if (node.status === 'CURRENT') return 'Begin'
  if (node.status === 'LOCKED') return 'Locked'
  return `${node.solvedCount}/${node.exerciseCount}`
}

export function LessonNode({ node }: { node: PathNode }) {
  const playable = node.status !== 'LOCKED'
  const kind = KIND_LABEL[node.nodeKind]

  const body = (
    <>
      <span className="path-seal" data-status={node.status} aria-hidden="true">
        {node.position}
      </span>

      <span style={{ display: 'grid', gap: '0.15rem', minWidth: 0 }}>
        <span
          className="brand-font"
          style={{
            fontSize: '1.08rem',
            fontWeight: 700,
            color: node.status === 'LOCKED' ? 'var(--muted)' : 'var(--text)',
          }}
        >
          {node.title}
        </span>
        <span
          style={{
            fontSize: '0.72rem',
            fontWeight: 700,
            letterSpacing: '0.09em',
            textTransform: 'uppercase',
            color: 'var(--muted)',
          }}
        >
          {kind}
        </span>
      </span>

      <span
        style={{
          fontSize: '0.72rem',
          fontWeight: 800,
          letterSpacing: '0.07em',
          textTransform: 'uppercase',
          whiteSpace: 'nowrap',
          color:
            node.status === 'CURRENT'
              ? 'var(--accent)'
              : node.status === 'COMPLETED'
                ? 'var(--brand)'
                : 'var(--muted)',
        }}
      >
        {statusNote(node)}
      </span>
    </>
  )

  return (
    <li
      className="path-item"
      data-reached={node.status !== 'LOCKED' ? 'true' : 'false'}
      style={{ opacity: playable ? 1 : 0.6 }}
    >
      {playable ? (
        <Link
          className="path-link"
          href={`/lesson/${node.lessonId}`}
          aria-label={`Lesson ${node.position}: ${node.title} — ${
            node.status === 'CURRENT' ? 'begin' : 'practise again'
          }`}
        >
          {body}
        </Link>
      ) : (
        body
      )}
    </li>
  )
}
