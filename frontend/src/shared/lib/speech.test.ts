import { describe, expect, it } from 'vitest'
import { isSyriacScript, toHebrewScript } from './speech'

/**
 * Syriac and Hebrew are the same Aramaic abjad in two hands, so a Hebrew voice
 * can pronounce our content properly if we hand it Hebrew letters. These cover
 * that the rewrite is faithful — a wrong letter here is a mispronounced lesson.
 */

describe('toHebrewScript', () => {
  it('maps the words the curriculum already teaches', () => {
    expect(toHebrewScript('ܫܠܡܐ')).toBe('שלמא') // shlomo — peace
    expect(toHebrewScript('ܚܕ')).toBe('חד') // had — one
    expect(toHebrewScript('ܬܠܬܐ')).toBe('תלתא') // tloto — three
    expect(toHebrewScript('ܐܒܐ')).toBe('אבא') // abo — father
  })

  it('keeps word order and spacing in a phrase', () => {
    expect(toHebrewScript('ܐܝܬ ܠܝ ܠܚܡܐ')).toBe('אית לי לחמא') // I have bread
  })

  it('uses final forms at the end of a word', () => {
    expect(toHebrewScript('ܬܪܝܢ')).toBe('תרין') // trein — two, final nun
    expect(toHebrewScript('ܫܡܟ')).toBe('שמך') // shmakh — your name, final kaf
  })

  it('leaves a lone letter in its ordinary form', () => {
    // The alphabet page taps single glyphs to hear the letter's name, and a
    // final form would be named "kaf sofit" rather than "kaf".
    expect(toHebrewScript('ܟ')).toBe('כ')
    expect(toHebrewScript('ܢ')).toBe('נ')
    expect(toHebrewScript('ܐ')).toBe('א')
    expect(toHebrewScript('ܬ')).toBe('ת')
  })

  it('covers all 22 letters in order', () => {
    expect(toHebrewScript('ܐ ܒ ܓ ܕ ܗ ܘ ܙ ܚ ܛ ܝ ܟ ܠ ܡ ܢ ܣ ܥ ܦ ܨ ܩ ܪ ܫ ܬ')).toBe(
      'א ב ג ד ה ו ז ח ט י כ ל מ נ ס ע פ צ ק ר ש ת'
    )
  })

  it('drops vowel pointing rather than half-converting it', () => {
    expect(toHebrewScript('ܫܠܵܡܵܐ')).toBe('שלמא')
  })

  it('passes non-Syriac text through untouched', () => {
    expect(toHebrewScript('shlomo')).toBe('shlomo')
  })
})

describe('isSyriacScript', () => {
  it('recognises Syriac and rejects everything else', () => {
    expect(isSyriacScript('ܫܠܡܐ')).toBe(true)
    expect(isSyriacScript('shlomo')).toBe(false)
    expect(isSyriacScript('שלמא')).toBe(false)
    expect(isSyriacScript('')).toBe(false)
  })
})
