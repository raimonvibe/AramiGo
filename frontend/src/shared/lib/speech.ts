/**
 * Browser speech synthesis for pronunciation prompts.
 *
 * Honest caveat: the Web Speech API has no Classical Syriac voice, so this reads
 * the romanization with whatever voice the device has. It is a placeholder for
 * recorded audio, not a substitute for it — and on machines with no speech engine
 * installed (common on Linux) there is no voice at all, which is why callers get
 * told whether playback is actually available.
 */

export const NORMAL_RATE = 0.85
export const SLOW_RATE = 0.45

export type SpeechAvailability = 'ready' | 'unsupported' | 'no-voice'

function synth(): SpeechSynthesis | null {
  if (typeof window === 'undefined') return null
  return window.speechSynthesis ?? null
}

export function speechAvailability(): SpeechAvailability {
  const speech = synth()
  if (!speech) return 'unsupported'
  // Voices load asynchronously in some browsers; an empty list after load means
  // the device genuinely has no speech engine.
  return speech.getVoices().length > 0 ? 'ready' : 'no-voice'
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

export function speak(text: string, rate: number = NORMAL_RATE) {
  const speech = synth()
  if (!speech || !text) return

  speech.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.rate = rate
  speech.speak(utterance)
}
