import type { LearningPath, PathNode, PathUnit } from '@/shared/lib/api'

/**
 * The path is a contents page, so most of its layout questions are really
 * questions about one number: how far down the whole curriculum the learner has
 * got. Everything here answers from that single position, which is why the
 * ruled line stays continuous however many units are open or closed.
 */

export interface UnitSummary {
  /** Position in `path.units` — also the chapter number shown on the seal. */
  index: number
  unit: PathUnit
  completedCount: number
  lessonCount: number
  /** The learner has reached this chapter, so the rule is gold through it. */
  reached: boolean
  /** Holds the lesson the learner is on — the one chapter opened by default. */
  active: boolean
}

export interface Bookmark {
  lessonId: number
  title: string
  unitTitle: string
  sectionNumber: number
  unitNumber: number
  /** Position of the lesson within its unit. */
  position: number
  solvedCount: number
  exerciseCount: number
  /** Nothing started yet — the plate invites rather than resumes. */
  fresh: boolean
  /** Every lesson is done; the plate offers practice instead of progress. */
  finished: boolean
}

/**
 * The lesson the learner is on: the one in progress, else the last one
 * finished. Null only when the curriculum is empty.
 */
function currentNode(units: PathUnit[]): { unit: PathUnit; node: PathNode } | null {
  let lastCompleted: { unit: PathUnit; node: PathNode } | null = null
  for (const unit of units) {
    for (const node of unit.nodes) {
      if (node.status === 'CURRENT') return { unit, node }
      if (node.status === 'COMPLETED') lastCompleted = { unit, node }
    }
  }
  if (lastCompleted) return lastCompleted
  const first = units.find(unit => unit.nodes.length > 0)
  return first ? { unit: first, node: first.nodes[0] } : null
}

export function summariseUnits(path: LearningPath | null): UnitSummary[] {
  if (!path) return []
  const here = currentNode(path.units)
  const activeIndex = here ? path.units.indexOf(here.unit) : -1

  return path.units.map((unit, index) => ({
    index,
    unit,
    completedCount: unit.nodes.filter(node => node.status === 'COMPLETED').length,
    lessonCount: unit.nodes.length,
    // Chapters before the learner's own are behind them, so gold runs through
    // them too — a collapsed chapter must not break the rule.
    reached: activeIndex >= 0 && index <= activeIndex,
    active: index === activeIndex,
  }))
}

/** Lesson rows are gold up to and including the learner's own row. */
export function reachedLessonIndex(nodes: PathNode[]): number {
  const current = nodes.findIndex(node => node.status === 'CURRENT')
  if (current >= 0) return current
  let lastCompleted = -1
  nodes.forEach((node, index) => {
    if (node.status === 'COMPLETED') lastCompleted = index
  })
  return lastCompleted
}

export function bookmarkFor(path: LearningPath | null): Bookmark | null {
  if (!path) return null
  const here = currentNode(path.units)
  if (!here) return null

  const started = path.units.some(unit =>
    unit.nodes.some(node => node.status !== 'LOCKED' && node.solvedCount > 0),
  )
  const finished = path.units.every(unit =>
    unit.nodes.every(node => node.status === 'COMPLETED'),
  )

  return {
    lessonId: here.node.lessonId,
    title: here.node.title,
    unitTitle: here.unit.title,
    sectionNumber: here.unit.sectionNumber,
    unitNumber: here.unit.unitNumber,
    position: here.node.position,
    solvedCount: here.node.solvedCount,
    exerciseCount: here.node.exerciseCount,
    fresh: !started,
    finished,
  }
}
