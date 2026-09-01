import type { CSSProperties } from 'react'

/**
 * Per-unit gradient families from docs/COLOR.md.
 *
 * One global brand forever: every family starts at brand gold and only the
 * second stop changes, so a new unit never invents a one-off hex. Scrolling the
 * contents page should feel like moving through a book with different
 * illuminations rather than down a list.
 *
 * The gradient fills the chapter seal, which draws the chapter numeral in near
 * black (`#07130f`, see `.unit-seal` in globals.css). So a family is only
 * usable if the numeral still clears 4.5:1 against the gradient's **midpoint**,
 * where the glyph actually sits — a stop measured on its own says nothing. Every
 * family below is checked against that, and against the page background so the
 * seal reads as an object rather than a smudge.
 *
 * Anchoring on brand gold is what makes that work, and is not only a branding
 * choice. The plum family used to start at a darker gold (`#a67c52`) and failed
 * at 4.00:1; the plum itself was fine, and against `#c4a35a` it reaches 5.07.
 * A dim first stop drags the midpoint down whatever the second stop is.
 */
export const BRAND_GOLD = '#c4a35a'

/** Near black, as drawn on the seal by `.unit-seal` — the numeral must clear it. */
export const SEAL_INK = '#07130f'

export const FAMILIES = {
  /* second stop            numeral / page contrast at the midpoint */
  intro: '#3f9f84', //      6.56 / 6.10 — teal
  story: '#8b5e4b', //      5.35 / 4.97 — clay, umber
  script: '#5a7d8c', //     5.77 / 5.37 — dusty ink-blue, not Macaw
  vine: '#546331', //       4.91 / 4.57 — olive, well clear of Duo's lime
  dusk: '#545d96', //       4.94 / 4.59 — indigo
  pomegranate: '#914b54', // 4.97 / 4.62 — muted garnet, not Cardinal
  liturgy: '#6b5b8c', //    5.07 / 4.70 — muted plum, not Beetle
} as const

export type UnitFamily = keyof typeof FAMILIES

/**
 * Rotates warm and cool alternately, so two chapters sitting a few rows apart on
 * the contents page never read as the same illumination. Seven families is 35
 * lessons before any colour repeats.
 */
const ORDER: UnitFamily[] = [
  'intro',
  'story',
  'script',
  'vine',
  'dusk',
  'pomegranate',
  'liturgy',
]

export function unitFamily(unitNumber: number): UnitFamily {
  return ORDER[(unitNumber - 1) % ORDER.length]
}

/** CSS custom properties to hang on a unit's row. */
export function unitThemeVars(unitNumber: number): CSSProperties {
  return {
    '--unit-from': BRAND_GOLD,
    '--unit-to': FAMILIES[unitFamily(unitNumber)],
  } as CSSProperties
}
