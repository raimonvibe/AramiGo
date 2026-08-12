import Link from 'next/link'
import { ImagePlate } from '@/shared/ui'
import { artForUnit } from '@/shared/lib/gameArt'
import type { Bookmark as BookmarkModel } from '../pathModel'

/**
 * Where the learner left off, kept at the top of the page.
 *
 * This is the whole app in one card: it is the same height whether the
 * curriculum has two units or fifty, so the one thing a returning learner needs
 * never moves further down the page as lessons are added.
 */
export function Bookmark({ bookmark }: { bookmark: BookmarkModel }) {
  const art = artForUnit(bookmark.sectionNumber, bookmark.unitNumber)
  const eyebrow = bookmark.finished ? 'Practise again' : bookmark.fresh ? 'Begin' : 'Continue'

  const partly = bookmark.solvedCount > 0 && bookmark.solvedCount < bookmark.exerciseCount
  const progress = partly
    ? ` · ${bookmark.solvedCount} of ${bookmark.exerciseCount} done`
    : ''

  return (
    <Link
      href={`/lesson/${bookmark.lessonId}`}
      className="bookmark"
      aria-label={`${eyebrow}: lesson ${bookmark.position}, ${bookmark.title}`}
    >
      <span className="bookmark-copy">
        <span className="bookmark-eyebrow">{eyebrow}</span>
        <span className="bookmark-title brand-font">{bookmark.title}</span>
        <span className="bookmark-meta">
          {bookmark.unitTitle} · lesson {bookmark.position}
          {progress}
        </span>
      </span>
      <ImagePlate src={art.src} alt={art.alt} size="xs" decorative />
    </Link>
  )
}
