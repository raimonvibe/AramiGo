'use client'

import { CaretIcon, LockIcon } from '@/shared/ui/icons'
import { reachedLessonIndex } from '../pathModel'
import type { UnitSummary } from '../pathModel'
import { unitThemeVars } from '../unitTheme'
import { LessonNode } from './LessonNode'

const ROMAN = [
  ['', 'M', 'MM', 'MMM'],
  ['', 'C', 'CC', 'CCC', 'CD', 'D', 'DC', 'DCC', 'DCCC', 'CM'],
  ['', 'X', 'XX', 'XXX', 'XL', 'L', 'LX', 'LXX', 'LXXX', 'XC'],
  ['', 'I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX'],
]

/**
 * Chapters are numbered in Roman on the seal and lessons in Arabic, so a glance
 * at the rule tells you which of the two you are looking at.
 */
export function roman(value: number): string {
  if (value < 1 || value > 3999) return String(value)
  const digits = String(value).padStart(4, '0').split('').map(Number)
  return digits.map((digit, place) => ROMAN[place][digit]).join('')
}

function summaryLine(summary: UnitSummary): string {
  const { completedCount, lessonCount, unit } = summary
  const lessons = `${lessonCount} ${lessonCount === 1 ? 'lesson' : 'lessons'}`

  if (!summary.reached) return `Section ${unit.sectionNumber} · ${lessons}`
  if (completedCount === lessonCount) return `Section ${unit.sectionNumber} · complete`
  return `Section ${unit.sectionNumber} · ${completedCount} of ${lessonCount} done`
}

/**
 * A single line of the contents page, which opens into the chapter.
 *
 * Collapsed is the default state for every chapter but the learner's own: a
 * closed chapter costs one row rather than a banner and every lesson, which is
 * what keeps the page from growing without bound as units are added.
 */
export function UnitRow({
  summary,
  open,
  onToggle,
}: {
  summary: UnitSummary
  open: boolean
  onToggle: () => void
}) {
  const { unit, index, reached } = summary
  const panelId = `unit-panel-${unit.sectionNumber}-${unit.unitNumber}`
  const reachedThrough = reachedLessonIndex(unit.nodes)

  return (
    <li className="contents-unit" style={unitThemeVars(unit.unitNumber)}>
      {/* Named explicitly: the seal is decorative, so the name computed from
          content would run the chapter numeral into the title. */}
      <button
        type="button"
        className="unit-row"
        data-reached={reached}
        aria-expanded={open}
        aria-controls={panelId}
        aria-label={`Chapter ${index + 1}, ${unit.title} — ${summaryLine(summary)}`}
        onClick={onToggle}
      >
        <span className="unit-seal brand-font" data-reached={reached} aria-hidden="true">
          {roman(index + 1)}
        </span>
        <span className="unit-copy">
          <span className="unit-title brand-font">{unit.title}</span>
          <span className="unit-meta">{summaryLine(summary)}</span>
        </span>
        <span className="unit-aside">
          {!reached && <LockIcon />}
          <CaretIcon className="unit-caret" />
        </span>
      </button>

      {open && (
        <div className="unit-open" id={panelId}>
          <p className="unit-description" data-reached={reached}>
            {unit.description}
          </p>
          <ol className="path-list" aria-label={`Lessons in ${unit.title}`}>
            {unit.nodes.map((node, nodeIndex) => (
              <LessonNode
                key={node.lessonId}
                node={node}
                reached={reached && nodeIndex <= reachedThrough}
              />
            ))}
          </ol>
        </div>
      )}
    </li>
  )
}
