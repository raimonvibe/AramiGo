'use client'

import { useCallback, useEffect, useRef, useState } from 'react'
import {
  NORMAL_RATE,
  SLOW_RATE,
  onVoicesChanged,
  speak,
  speakSequence,
  speechNotice,
  type SpeechAvailability,
} from '@/shared/lib/speech'
import { PlayIcon, SlowPlayIcon } from '@/shared/ui/icons'

export interface SyriacNumber {
  value: number
  /** The letter that carries this value when Syriac is written as numerals. */
  numeral: string
  /** The spoken number word, in script. */
  word: string
  transliteration: string
}

export function NumbersSection({ numbers }: { numbers: SyriacNumber[] }) {
  const [voices, setVoices] = useState<SpeechAvailability>('ready')
  const [playingIndex, setPlayingIndex] = useState(-1)
  const stopRef = useRef<(() => void) | null>(null)

  useEffect(() => onVoicesChanged(setVoices), [])

  const stop = useCallback(() => {
    stopRef.current?.()
    stopRef.current = null
  }, [])

  // Never leave a voice talking to a page the learner has navigated away from.
  useEffect(() => stop, [stop])

  const playAll = useCallback(() => {
    stop()
    stopRef.current = speakSequence(
      numbers.map(number => number.transliteration),
      {
        onIndex: setPlayingIndex,
        onDone: () => {
          stopRef.current = null
        },
      }
    )
  }, [numbers, stop])

  const playOne = useCallback(
    (transliteration: string, rate: number) => {
      stop()
      setPlayingIndex(-1)
      speak(transliteration, rate)
    },
    [stop]
  )

  const ready = voices === 'ready'
  const running = playingIndex >= 0
  const notice = ready ? speechNotice() : null

  return (
    <section style={{ marginTop: '2.5rem' }}>
      <h2 className="brand-font" style={{ fontSize: '1.5rem', margin: '0 0 0.35rem' }}>
        Numbers
      </h2>
      <p style={{ color: 'var(--muted)', margin: '0 0 1rem', lineHeight: 1.5 }}>
        Syriac has no separate digits — the letters double as numerals, so{' '}
        <span className="syriac-inline" style={{ fontSize: '1.25rem' }}>
          ܐ
        </span>{' '}
        is also 1 and{' '}
        <span className="syriac-inline" style={{ fontSize: '1.25rem' }}>
          ܝ
        </span>{' '}
        is also 10. Each row below shows the numeral, the word you say, and how it sounds.
      </p>

      <button
        type="button"
        onClick={running ? stop : playAll}
        disabled={!ready}
        style={{
          border: 'none',
          borderRadius: 14,
          padding: '0.6rem 1.1rem',
          fontWeight: 800,
          background: ready ? 'var(--brand)' : '#3a4750',
          color: ready ? '#1a160c' : '#7d8d97',
          marginBottom: '1rem',
        }}
      >
        {running ? 'Stop' : 'Read all one by one'}
      </button>

      <ol
        style={{
          listStyle: 'none',
          margin: 0,
          padding: 0,
          display: 'grid',
          gap: '0.6rem',
        }}
      >
        {numbers.map((number, index) => {
          const active = index === playingIndex
          return (
            <li
              key={number.value}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.9rem',
                flexWrap: 'wrap',
                padding: '0.7rem 0.9rem',
                borderRadius: 14,
                border: `1px solid ${active ? 'var(--brand)' : 'var(--line)'}`,
                background: active ? 'rgba(196, 163, 90, 0.12)' : 'var(--bg-elevated)',
              }}
            >
              <span
                aria-hidden="true"
                style={{
                  minWidth: '2.1rem',
                  fontWeight: 800,
                  color: 'var(--muted)',
                  fontVariantNumeric: 'tabular-nums',
                }}
              >
                {number.value}
              </span>
              <span className="syriac-inline" style={{ fontSize: '1.9rem' }}>
                {number.word}
              </span>
              <span style={{ color: 'var(--muted)', fontStyle: 'italic' }}>
                {number.transliteration}
              </span>
              <span style={{ color: 'var(--muted)', fontSize: '0.85rem' }}>
                numeral{' '}
                <span className="syriac-inline" style={{ fontSize: '1.15rem' }}>
                  {number.numeral}
                </span>
              </span>

              <span style={{ display: 'flex', gap: '0.5rem', marginLeft: 'auto' }}>
                <button
                  type="button"
                  onClick={() => playOne(number.transliteration, NORMAL_RATE)}
                  disabled={!ready}
                  aria-label={`Play ${number.value}`}
                  style={{
                    width: 42,
                    height: 42,
                    borderRadius: 12,
                    border: 'none',
                    display: 'grid',
                    placeItems: 'center',
                    background: ready ? 'var(--brand)' : '#3a4750',
                    color: ready ? '#1a160c' : '#7d8d97',
                  }}
                >
                  <PlayIcon size={20} />
                </button>
                <button
                  type="button"
                  onClick={() => playOne(number.transliteration, SLOW_RATE)}
                  disabled={!ready}
                  aria-label={`Play ${number.value} slowly`}
                  style={{
                    width: 42,
                    height: 42,
                    borderRadius: 12,
                    border: 'none',
                    display: 'grid',
                    placeItems: 'center',
                    background: ready ? 'var(--accent)' : '#3a4750',
                    color: ready ? '#07130f' : '#7d8d97',
                  }}
                >
                  <SlowPlayIcon size={18} />
                </button>
              </span>
            </li>
          )
        })}
      </ol>

      {notice && (
        <p style={{ color: 'var(--muted)', fontSize: '0.8rem', marginTop: '0.9rem', lineHeight: 1.45 }}>
          {notice}
        </p>
      )}
      {!ready && (
        <p style={{ color: 'var(--muted)', fontSize: '0.8rem', marginTop: '0.9rem', lineHeight: 1.45 }}>
          {voices === 'unsupported'
            ? 'This browser has no speech engine, so the numbers cannot be read aloud here.'
            : 'No system voice is installed yet, so the numbers cannot be read aloud here.'}
        </p>
      )}
    </section>
  )
}
