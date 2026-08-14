import { describe, expect, it } from 'vitest'
import { GAME_ART, artForUnit } from './gameArt'

/**
 * The portrait pack is fixed and the curriculum is not, so `artForUnit` rotates
 * rather than mapping one unit to one image forever. These guard the properties
 * that make the rotation safe to leave alone as units are added — not the
 * specific pairings, which are free to change.
 */

const POOL_SIZE = 9

describe('unit portraits', () => {
  it('keeps the portraits units 1-5 were curated with', () => {
    expect(artForUnit(1).src).toBe(GAME_ART.harvest)
    expect(artForUnit(2).src).toBe(GAME_ART.family)
    expect(artForUnit(3).src).toBe(GAME_ART.unlock)
    expect(artForUnit(4).src).toBe(GAME_ART.welcome)
    expect(artForUnit(5).src).toBe(GAME_ART.vigil)
  })

  it('gives every unit a portrait, however far the curriculum runs', () => {
    for (let unit = 1; unit <= 50; unit++) {
      expect(artForUnit(unit).src).toBeTruthy()
      expect(artForUnit(unit).alt).toBeTruthy()
    }
  })

  it('never repeats a portrait inside one pass of the pool', () => {
    const seen = new Set<string>()
    for (let unit = 1; unit <= POOL_SIZE; unit++) {
      seen.add(artForUnit(unit).src)
    }
    expect(seen.size).toBe(POOL_SIZE)
  })

  it('wraps once the pool has been used', () => {
    expect(artForUnit(POOL_SIZE + 1)).toEqual(artForUnit(1))
    expect(artForUnit(POOL_SIZE + 2)).toEqual(artForUnit(2))
  })

  /**
   * The gradient families rotate every 7 and portraits every 9. If a portrait
   * ever came back on the same cycle as its colour, two chapters would look
   * wholly identical rather than merely sharing one attribute.
   */
  it('does not return a portrait in step with the colour it first appeared in', () => {
    expect(POOL_SIZE % 7).not.toBe(0)
  })

  it('keeps the Hebrew tablets away from Syriac lessons', () => {
    for (let unit = 1; unit <= 50; unit++) {
      expect(artForUnit(unit).src).not.toContain('game-image14')
    }
  })
})
