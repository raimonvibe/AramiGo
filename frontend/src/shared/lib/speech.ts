/**
 * Browser speech for listen prompts.
 *
 * Classical Syriac has no system TTS voice. We prefer a **Hebrew** voice + `he-IL`
 * so Semitic consonants come closer than an English default. The engine still
 * reads our Latin romanization (e.g. "shlomo") — this is an approximation, not
 * authentic Syriac or Galilean Aramaic. Recorded clips remain the long-term plan.
 *
 * On machines with no speech engine (common on Linux) playback is unavailable.
 */

export const NORMAL_RATE = 0.85
export const SLOW_RATE = 0.45

/** BCP 47 tag we ask the engine to use for Syriac romanization stand-ins. */
export const SPEECH_LANG = 'he-IL'

export type SpeechAvailability = 'ready' | 'unsupported' | 'no-voice'

/** Which kind of voice we will actually use right now. */
export type SpeechVoiceKind = 'hebrew' | 'other' | 'none'

export const SPEECH_NOTICE_HEBREW =
  'Listen mode uses a Hebrew system voice as a stand-in. Classical Syriac has no built-in speech engine — this is an approximation, not authentic Syriac.'

export const SPEECH_NOTICE_FALLBACK =
  'No Hebrew voice on this device, so another system voice is used. Classical Syriac has no built-in speech engine — pronunciation is only a rough stand-in.'

/**
 * Syriac letter -> Hebrew letter.
 *
 * Both scripts are the same 22-letter Aramaic abjad in the same order, so this
 * is a one-to-one map rather than a transliteration scheme. Handed ܚܕ as חד, a
 * Hebrew voice pronounces a Semitic word; handed the Latin "had", it reads
 * English.
 */
const SYRIAC_TO_HEBREW: Record<string, string> = {
  'ܐ': 'א', // alaph -> alef
  'ܒ': 'ב', // beth -> bet
  'ܓ': 'ג', // gamal -> gimel
  'ܕ': 'ד', // dalath -> dalet
  'ܖ': 'ד', // dotless dalath -> dalet
  'ܗ': 'ה', // he -> he
  'ܘ': 'ו', // waw -> vav
  'ܙ': 'ז', // zayn -> zayin
  'ܚ': 'ח', // heth -> het
  'ܛ': 'ט', // teth -> tet
  'ܝ': 'י', // yudh -> yod
  'ܟ': 'כ', // kaph -> kaf
  'ܠ': 'ל', // lamadh -> lamed
  'ܡ': 'מ', // mim -> mem
  'ܢ': 'נ', // nun -> nun
  'ܣ': 'ס', // semkath -> samekh
  'ܤ': 'ס', // final semkath -> samekh
  'ܥ': 'ע', // e -> ayin
  'ܦ': 'פ', // pe -> pe
  'ܨ': 'צ', // sadhe -> tsadi
  'ܩ': 'ק', // qaph -> qof
  'ܪ': 'ר', // rish -> resh
  'ܫ': 'ש', // shin -> shin
  'ܬ': 'ת', // taw -> tav
}

/** Hebrew letters that take a different shape at the end of a word. */
const HEBREW_FINALS: Record<string, string> = {
  'כ': 'ך', // kaf
  'מ': 'ם', // mem
  'נ': 'ן', // nun
  'פ': 'ף', // pe
  'צ': 'ץ', // tsadi
}

/** True when the text contains any Syriac character. */
export function isSyriacScript(text: string): boolean {
  return /[܀-ݏ]/.test(text)
}

/**
 * Rewrites Syriac script into Hebrew script for the speech engine.
 *
 * Syriac vowel points are dropped rather than converted to niqqud: the content
 * here is mostly unpointed, and a half-pointed word reads worse than a bare
 * consonantal one.
 */
export function toHebrewScript(syriac: string): string {
  const mapped = [...syriac]
    .map(char => SYRIAC_TO_HEBREW[char] ?? (isSyriacScript(char) ? '' : char))
    .join('')

  // Final forms can only be decided once the whole word is known.
  return mapped
    .split(/(\s+)/)
    .map(part => {
      // A lone letter is being named, not ending a word — the alphabet page
      // taps single glyphs, and "kaf sofit" is the wrong name for kaf.
      if (!part.trim() || part.length < 2) return part
      const last = part[part.length - 1]
      const final = HEBREW_FINALS[last]
      return final ? part.slice(0, -1) + final : part
    })
    .join('')
}

/**
 * What to actually hand the engine for a word we can write both ways.
 *
 * A Hebrew voice does far better with Hebrew letters than with our Latin
 * romanization, but on a device that fell back to an English voice the
 * romanization is the only thing it can make sense of at all.
 */
export function pronounceable(script: string | null | undefined, transliteration: string): string {
  if (script && isSyriacScript(script) && speechVoiceKind() === 'hebrew') {
    return toHebrewScript(script)
  }
  return transliteration
}

function synth(): SpeechSynthesis | null {
  if (typeof window === 'undefined') return null
  return window.speechSynthesis ?? null
}

function isHebrewVoice(voice: SpeechSynthesisVoice): boolean {
  const lang = voice.lang.toLowerCase()
  // `iw` is the old ISO code some engines still report for Hebrew.
  return lang.startsWith('he') || lang.startsWith('iw')
}

function allVoices(): SpeechSynthesisVoice[] {
  const speech = synth()
  if (!speech) return []
  return speech.getVoices()
}

/** Prefer Hebrew; otherwise any installed voice. */
export function preferredVoice(): SpeechSynthesisVoice | null {
  const voices = allVoices()
  if (voices.length === 0) return null
  return voices.find(isHebrewVoice) ?? voices[0] ?? null
}

export function speechVoiceKind(): SpeechVoiceKind {
  const voice = preferredVoice()
  if (!voice) return 'none'
  return isHebrewVoice(voice) ? 'hebrew' : 'other'
}

export function speechAvailability(): SpeechAvailability {
  const speech = synth()
  if (!speech) return 'unsupported'
  return allVoices().length > 0 ? 'ready' : 'no-voice'
}

export function speechNotice(): string | null {
  const kind = speechVoiceKind()
  if (kind === 'hebrew') return SPEECH_NOTICE_HEBREW
  if (kind === 'other') return SPEECH_NOTICE_FALLBACK
  return null
}

/** Runs `onChange` whenever the voice list resolves, so the UI can stop guessing. */
export function onVoicesChanged(onChange: (availability: SpeechAvailability) => void): () => void {
  const speech = synth()
  if (!speech) {
    onChange('unsupported')
    return () => {}
  }

  const notify = () => onChange(speechAvailability())
  notify()
  speech.addEventListener('voiceschanged', notify)
  return () => speech.removeEventListener('voiceschanged', notify)
}

function buildUtterance(text: string, rate: number): SpeechSynthesisUtterance {
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.rate = rate
  utterance.lang = SPEECH_LANG

  const voice = preferredVoice()
  if (voice) {
    utterance.voice = voice
    // Keep lang aligned with the chosen voice when we fell back to non-Hebrew.
    if (!isHebrewVoice(voice)) {
      utterance.lang = voice.lang || SPEECH_LANG
    }
  }

  return utterance
}

export function speak(text: string, rate: number = NORMAL_RATE) {
  const speech = synth()
  if (!speech || !text) return

  speech.cancel()
  speech.speak(buildUtterance(text, rate))
}

export interface SpeakSequenceOptions {
  rate?: number
  /** Index now being spoken, or -1 once the run has finished or been stopped. */
  onIndex?: (index: number) => void
  onDone?: () => void
}

/**
 * Reads a list aloud in order, one item at a time.
 *
 * Chained through `onend` rather than handed to the engine in one batch, so the
 * caller can follow along and stopping actually stops — cancelling a queue the
 * engine already accepted is unreliable across browsers.
 *
 * Returns a stop function; calling it is safe at any point, including after the
 * run has already finished.
 */
export function speakSequence(
  texts: string[],
  options: SpeakSequenceOptions = {}
): () => void {
  const speech = synth()
  const spoken = texts.filter(Boolean)

  if (!speech || spoken.length === 0) {
    options.onDone?.()
    return () => {}
  }

  const rate = options.rate ?? NORMAL_RATE
  let stopped = false
  let next = 0

  const finish = () => {
    if (stopped) return
    stopped = true
    options.onIndex?.(-1)
    options.onDone?.()
  }

  const speakNext = () => {
    if (stopped) return
    if (next >= spoken.length) return finish()

    const index = next++
    options.onIndex?.(index)

    const utterance = buildUtterance(spoken[index], rate)
    // Advance on error too, so one unpronounceable entry cannot strand the run.
    utterance.onend = speakNext
    utterance.onerror = speakNext
    speech.speak(utterance)
  }

  // Clear anything already talking before we own the queue. Our own utterances
  // do not exist yet, so no handler of ours can fire from this.
  speech.cancel()
  speakNext()

  return () => {
    if (stopped) return
    stopped = true
    speech.cancel()
    options.onIndex?.(-1)
    options.onDone?.()
  }
}
