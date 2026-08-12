import type { CSSProperties } from 'react'

/**
 * Per-unit gradient families from docs/COLOR.md.
 *
 * One global brand forever; only `--unit-from` / `--unit-to` change, so a new
 * unit never invents a one-off hex. Scrolling the contents page should feel like
 * moving through a book with different illuminations rather than down a list.
 */
const FAMILIES = {
  intro: { from: '#c4a35a', to: '#3f9f84' },
  script: { from: '#b8956a', to: '#5a7d8c' },
  liturgy: { from: '#a67c52', to: '#6b5b8c' },
  story: { from: '#c4a35a', to: '#8b5e4b' },
} as const

export type UnitFamily = keyof typeof FAMILIES

/** Rotates through the families so units added later still get a theme. */
const ORDER: UnitFamily[] = ['intro', 'script', 'story', 'liturgy']

export function unitFamily(unitNumber: number): UnitFamily {
  return ORDER[(unitNumber - 1) % ORDER.length]
}

/** CSS custom properties to hang on a unit's row. */
export function unitThemeVars(unitNumber: number): CSSProperties {
  const family = FAMILIES[unitFamily(unitNumber)]
  return { '--unit-from': family.from, '--unit-to': family.to } as CSSProperties
}
