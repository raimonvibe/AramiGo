import Link from 'next/link'
import type { NodeKind, PathNode } from '@/shared/lib/api'

const KIND_LABEL: Record<NodeKind, string> = {
  STAR: 'Lesson',
  CHEST: 'Treasure',
  CHARACTER: 'Practice',
}

function statusLabel(node: PathNode): string {
  const kind = KIND_LABEL[node.nodeKind]
  const partly = node.solvedCount > 0 && node.solvedCount < node.exerciseCount

  if (node.status === 'LOCKED') return `${kind} · Locked`
  if (partly) return `${kind} · ${node.solvedCount}/${node.exerciseCount}`
  if (node.status === 'CURRENT') return `Current · ${kind}`
  if (node.status === 'COMPLETED') return `${kind} · Done`
  return kind
}

/**
 * One chapter row: a numbered Literata seal on the rule, the title, and the
 * node's kind in words. No pictograms and no serpentine offset — the rule
 * carries progress. See docs/COLOR.md.
 */
export function LessonNode({ node, reached }: { node: PathNode; reached: boolean }) {
  const playable = node.status !== 'LOCKED'

  const body = (
    <>
      <span className="path-seal brand-font" data-status={node.status} aria-hidden="true">
        {node.position}
      </span>
      <span className="path-title" style={{ color: playable ? 'var(--text)' : 'var(--muted)' }}>
        {node.title}
      </span>
      <span className="path-meta" data-status={node.status}>
        {statusLabel(node)}
      </span>
    </>
  )

  if (!playable) {
    // No link and no aria-disabled: a locked row is plain text, and its own
    // "Locked" meta says so. The seal is decorative, so the number is spoken
    // here instead.
    return (
      <li className="path-item" data-reached={reached} style={{ opacity: 0.58 }}>
        <span className="visually-hidden">Lesson {node.position}:</span>
        {body}
      </li>
    )
  }

  return (
    <li className="path-item" data-reached={reached}>
      <Link
        href={`/lesson/${node.lessonId}`}
        className="path-link"
        aria-label={`Lesson ${node.position}: ${node.title} — ${
          node.status === 'CURRENT' ? 'start' : 'practice again'
        }`}
      >
        {body}
      </Link>
    </li>
  )
}
