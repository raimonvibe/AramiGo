import { describe, expect, it } from 'vitest'
import { BRAND_GOLD, FAMILIES, SEAL_INK, unitFamily, unitThemeVars } from './unitTheme'

/**
 * The chapter seal draws its numeral in near black over the unit gradient, so a
 * family is only usable if the numeral survives it. This is easy to get wrong by
 * eye: the plum shipped at 4.00:1 because each stop looked fine measured alone,
 * while the glyph actually sits at the midpoint. These guard the rule rather
 * than the specific hexes — pick any colour you like that passes.
 */

const PAGE_BACKGROUND = '#121a1f'

function channels(hex: string): [number, number, number] {
  return [1, 3, 5].map(i => parseInt(hex.slice(i, i + 2), 16)) as [number, number, number]
}

function relativeLuminance(hex: string): number {
  const [r, g, b] = channels(hex).map(value => {
    const v = value / 255
    return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4)
  })
  return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

function contrast(a: string, b: string): number {
  const [lighter, darker] = [relativeLuminance(a), relativeLuminance(b)].sort((x, y) => y - x)
  return (lighter + 0.05) / (darker + 0.05)
}

/** Where the numeral actually sits on a 135deg two-stop gradient. */
function midpoint(a: string, b: string): string {
  const other = channels(b)
  return (
    '#' +
    channels(a)
      .map((value, i) => Math.round((value + other[i]) / 2).toString(16).padStart(2, '0'))
      .join('')
  )
}

function hue(hex: string): number {
  const [r, g, b] = channels(hex).map(v => v / 255)
  const max = Math.max(r, g, b)
  const delta = max - Math.min(r, g, b)
  if (delta === 0) return 0
  const sextant =
    max === r ? ((g - b) / delta) % 6 : max === g ? (b - r) / delta + 2 : (r - g) / delta + 4
  return ((sextant * 60) + 360) % 360
}

function hueGap(a: string, b: string): number {
  const gap = Math.abs(hue(a) - hue(b))
  return Math.min(gap, 360 - gap)
}

const families = Object.entries(FAMILIES)

describe('unit gradients', () => {
  it.each(families)('%s keeps the chapter numeral readable', (_name, stop) => {
    // WCAG 4.5:1 — the numeral is small text, so AA-large is not enough.
    expect(contrast(SEAL_INK, midpoint(BRAND_GOLD, stop))).toBeGreaterThanOrEqual(4.5)
  })

  it.each(families)('%s keeps the seal distinct from the page', (_name, stop) => {
    expect(contrast(PAGE_BACKGROUND, midpoint(BRAND_GOLD, stop))).toBeGreaterThanOrEqual(3)
  })

  it('keeps families far enough apart to tell chapters apart', () => {
    const tooClose = families.flatMap(([nameA, a], i) =>
      families.slice(i + 1).flatMap(([nameB, b]) => {
        const gap = hueGap(a, b)
        return gap < 25 ? [`${nameA}/${nameB} ${Math.round(gap)}deg`] : []
      }),
    )
    expect(tooClose).toEqual([])
  })
})

describe('rotation', () => {
  it('gives the first units their documented families', () => {
    expect(unitFamily(1)).toBe('intro')
    expect(unitFamily(2)).toBe('story')
    expect(unitFamily(3)).toBe('script')
  })

  it('wraps once every family has been used', () => {
    const count = families.length
    expect(unitFamily(count + 1)).toBe(unitFamily(1))
    expect(unitFamily(count + 2)).toBe(unitFamily(2))
  })

  it('always anchors on the one brand gold', () => {
    for (let unit = 1; unit <= families.length * 2; unit++) {
      expect(unitThemeVars(unit)).toMatchObject({ '--unit-from': BRAND_GOLD })
    }
  })
})
