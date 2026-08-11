'use client'

import { useReadAloud } from '../useReadAloud'
import { ReadAllButton } from './ReadAllButton'

export interface SyriacLetter {
  glyph: string
  name: string
  /** What the letter is worth when Syriac is written as numerals. */
  value: number
}

export function AlphabetGrid({ letters }: { letters: SyriacLetter[] }) {
  const audio = useReadAloud(letters.map(letter => letter.name))

  return (
    <>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.9rem',
          flexWrap: 'wrap',
          marginTop: '1.5rem',
        }}
      >
        <ReadAllButton
          ready={audio.ready}
          running={audio.running}
          label="Read all one by one"
          onPlayAll={audio.playAll}
          onStop={audio.stop}
        />
        <p style={{ color: 'var(--muted)', margin: 0, fontSize: '0.9rem' }}>
          Tap a letter to hear its name.
        </p>
      </div>

      <ul className="alphabet-grid">
        {letters.map((letter, index) => {
          const active = index === audio.playingIndex
          return (
            <li key={letter.name}>
              <button
                type="button"
                onClick={() => audio.playOne(letter.name)}
                disabled={!audio.ready}
                aria-label={`Hear ${letter.name}`}
                style={{
                  width: '100%',
                  display: 'grid',
                  gap: '0.25rem',
                  justifyItems: 'center',
                  padding: '0.85rem 0.5rem',
                  borderRadius: 14,
                  border: `1px solid ${active ? 'var(--brand)' : 'var(--line)'}`,
                  background: active ? 'rgba(196, 163, 90, 0.12)' : 'var(--bg-elevated)',
                  color: 'var(--text)',
                  font: 'inherit',
                  cursor: audio.ready ? 'pointer' : 'default',
                }}
              >
                <span className="syriac" style={{ fontSize: '2.1rem' }}>
                  {letter.glyph}
                </span>
                <span style={{ fontSize: '0.82rem', color: 'var(--muted)', fontWeight: 700 }}>
                  {letter.name}
                </span>
                <span
                  style={{ fontSize: '0.72rem', color: 'var(--muted)', opacity: 0.75 }}
                  title={`${letter.name} stands for ${letter.value} when used as a numeral`}
                >
                  = {letter.value}
                </span>
              </button>
            </li>
          )
        })}
      </ul>
    </>
  )
}
