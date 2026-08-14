import { describe, expect, it } from 'vitest'
import type { LearningPath, NodeStatus, PathNode } from '@/shared/lib/api'
import { bookmarkFor, reachedLessonIndex, summariseUnits } from './pathModel'

/**
 * The contents page collapses chapters, so a learner can be looking at a closed
 * unit that the gold rule still has to run through. These cover that the rule
 * and the bookmark both answer from the learner's real position rather than
 * from whatever happens to be expanded.
 */

function node(position: number, status: NodeStatus, solved = 0): PathNode {
  return {
    lessonId: position * 100,
    position,
    title: `Lesson ${position}`,
    nodeKind: 'STAR',
    status,
    exerciseCount: 6,
    solvedCount: status === 'COMPLETED' ? 6 : solved,
  }
}

function path(units: PathNode[][]): LearningPath {
  return {
    stats: { energy: 5, maxEnergy: 5, gems: 0, streak: 0, secondsUntilNextEnergy: 0 },
    reviewDue: 0,
    units: units.map((nodes, index) => ({
      sectionNumber: 1,
      unitNumber: index + 1,
      title: `Unit ${index + 1}`,
      description: `About unit ${index + 1}`,
      nodes,
    })),
  }
}

describe('summariseUnits', () => {
  it('opens the chapter holding the current lesson, not the first one', () => {
    const summaries = summariseUnits(
      path([
        [node(1, 'COMPLETED'), node(2, 'COMPLETED')],
        [node(1, 'CURRENT'), node(2, 'LOCKED')],
        [node(1, 'LOCKED')],
      ]),
    )

    expect(summaries.map(s => s.active)).toEqual([false, true, false])
  })

  it('runs gold through finished chapters so a collapsed unit does not break the rule', () => {
    const summaries = summariseUnits(
      path([
        [node(1, 'COMPLETED')],
        [node(1, 'CURRENT')],
        [node(1, 'LOCKED')],
      ]),
    )

    expect(summaries.map(s => s.reached)).toEqual([true, true, false])
  })

  it('counts finished lessons per chapter for the collapsed summary', () => {
    const summaries = summariseUnits(
      path([[node(1, 'COMPLETED'), node(2, 'COMPLETED'), node(3, 'CURRENT')]]),
    )

    expect(summaries[0].completedCount).toBe(2)
    expect(summaries[0].lessonCount).toBe(3)
  })

  it('opens the first chapter for a learner who has not started', () => {
    const summaries = summariseUnits(path([[node(1, 'CURRENT')], [node(1, 'LOCKED')]]))

    expect(summaries[0].active).toBe(true)
    expect(summaries[0].reached).toBe(true)
  })

  it('has nothing to summarise before the path loads', () => {
    expect(summariseUnits(null)).toEqual([])
  })
})

describe('reachedLessonIndex', () => {
  it('reaches through the current lesson', () => {
    expect(reachedLessonIndex([node(1, 'COMPLETED'), node(2, 'CURRENT'), node(3, 'LOCKED')])).toBe(1)
  })

  it('reaches through the last completed lesson when none is current', () => {
    expect(reachedLessonIndex([node(1, 'COMPLETED'), node(2, 'COMPLETED')])).toBe(1)
  })

  it('reaches nothing in a chapter the learner has not opened yet', () => {
    expect(reachedLessonIndex([node(1, 'LOCKED'), node(2, 'LOCKED')])).toBe(-1)
  })
})

describe('bookmarkFor', () => {
  it('points at the lesson in progress and carries its exercise count', () => {
    const mark = bookmarkFor(
      path([[node(1, 'COMPLETED')], [node(2, 'CURRENT', 2), node(3, 'LOCKED')]]),
    )

    expect(mark?.lessonId).toBe(200)
    expect(mark?.position).toBe(2)
    expect(mark?.solvedCount).toBe(2)
    expect(mark?.unitTitle).toBe('Unit 2')
  })

  it('invites rather than resumes when nothing has been solved', () => {
    const mark = bookmarkFor(path([[node(1, 'CURRENT'), node(2, 'LOCKED')]]))

    expect(mark?.fresh).toBe(true)
    expect(mark?.finished).toBe(false)
    expect(mark?.lessonId).toBe(100)
  })

  it('offers the last lesson for practice once every lesson is done', () => {
    const mark = bookmarkFor(path([[node(1, 'COMPLETED'), node(2, 'COMPLETED')]]))

    expect(mark?.finished).toBe(true)
    expect(mark?.lessonId).toBe(200)
  })

  it('has nowhere to point before the path loads', () => {
    expect(bookmarkFor(null)).toBeNull()
  })
})
