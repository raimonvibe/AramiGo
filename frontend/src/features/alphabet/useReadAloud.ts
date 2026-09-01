'use client'

import { useCallback, useEffect, useRef, useState } from 'react'
import {
  NORMAL_RATE,
  onVoicesChanged,
  pronounceable,
  speak,
  speakSequence,
  type SpeechAvailability,
} from '@/shared/lib/speech'

/** Something the learner can hear, written both ways. */
export interface ReadAloudItem {
  /** Syriac script, when we have it — preferred on a Hebrew voice. */
  script?: string | null
  /** Latin romanization, used when no Hebrew voice is available. */
  transliteration: string
}

export interface ReadAloud {
  /** A voice exists, so the play controls can do something. */
  ready: boolean
  availability: SpeechAvailability
  /** True while a whole-list run is in progress. */
  running: boolean
  /** Index currently being spoken during a run, or -1. */
  playingIndex: number
  playAll: () => void
  playOne: (item: ReadAloudItem, rate?: number) => void
  stop: () => void
}

/**
 * Play controls for a list the learner can hear straight through or one entry
 * at a time.
 *
 * Which spelling actually gets spoken is decided at play time, not render time:
 * the voice list resolves asynchronously, so anything decided earlier would be
 * guessing about a voice the browser had not reported yet.
 */
export function useReadAloud(items: ReadAloudItem[]): ReadAloud {
  const [availability, setAvailability] = useState<SpeechAvailability>('ready')
  const [playingIndex, setPlayingIndex] = useState(-1)
  const stopRef = useRef<(() => void) | null>(null)

  useEffect(() => onVoicesChanged(setAvailability), [])

  const stop = useCallback(() => {
    stopRef.current?.()
    stopRef.current = null
  }, [])

  // Never leave a voice talking to a page the learner has left.
  useEffect(() => stop, [stop])

  const playAll = useCallback(() => {
    stop()
    stopRef.current = speakSequence(
      items.map(item => pronounceable(item.script, item.transliteration)),
      {
        onIndex: setPlayingIndex,
        onDone: () => {
          stopRef.current = null
        },
      }
    )
  }, [items, stop])

  const playOne = useCallback(
    (item: ReadAloudItem, rate: number = NORMAL_RATE) => {
      stop()
      setPlayingIndex(-1)
      speak(pronounceable(item.script, item.transliteration), rate)
    },
    [stop]
  )

  return {
    ready: availability === 'ready',
    availability,
    running: playingIndex >= 0,
    playingIndex,
    playAll,
    playOne,
    stop,
  }
}
