'use client'

import { useCallback, useEffect, useRef, useState } from 'react'
import {
  NORMAL_RATE,
  onVoicesChanged,
  speak,
  speakSequence,
  type SpeechAvailability,
} from '@/shared/lib/speech'

export interface ReadAloud {
  /** A voice exists, so the play controls can do something. */
  ready: boolean
  availability: SpeechAvailability
  /** True while a whole-list run is in progress. */
  running: boolean
  /** Index currently being spoken during a run, or -1. */
  playingIndex: number
  playAll: () => void
  playOne: (text: string, rate?: number) => void
  stop: () => void
}

/**
 * Play controls for a list the learner can hear straight through or one entry
 * at a time.
 *
 * Callers may build `texts` inline; a fresh array each render only re-creates
 * `playAll`, which nothing depends on beyond a click handler.
 */
export function useReadAloud(texts: string[]): ReadAloud {
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
    stopRef.current = speakSequence(texts, {
      onIndex: setPlayingIndex,
      onDone: () => {
        stopRef.current = null
      },
    })
  }, [texts, stop])

  const playOne = useCallback(
    (text: string, rate: number = NORMAL_RATE) => {
      stop()
      setPlayingIndex(-1)
      speak(text, rate)
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
