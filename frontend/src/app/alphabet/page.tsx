import { BottomNav } from '@/shared/ui'

/**
 * The 22 letters of the Syriac abjad in traditional order.
 *
 * Names only — pronunciation is deliberately left to the lessons, because East
 * and West Syriac differ on several letters and picking one here would quietly
 * teach a dialect the curriculum hasn't chosen yet.
 */
const LETTERS: { glyph: string; name: string }[] = [
  { glyph: 'ܐ', name: 'Alaph' },
  { glyph: 'ܒ', name: 'Beth' },
  { glyph: 'ܓ', name: 'Gamal' },
  { glyph: 'ܕ', name: 'Dalath' },
  { glyph: 'ܗ', name: 'He' },
  { glyph: 'ܘ', name: 'Waw' },
  { glyph: 'ܙ', name: 'Zayn' },
  { glyph: 'ܚ', name: 'Heth' },
  { glyph: 'ܛ', name: 'Teth' },
  { glyph: 'ܝ', name: 'Yudh' },
  { glyph: 'ܟ', name: 'Kaph' },
  { glyph: 'ܠ', name: 'Lamadh' },
  { glyph: 'ܡ', name: 'Mim' },
  { glyph: 'ܢ', name: 'Nun' },
  { glyph: 'ܣ', name: 'Semkath' },
  { glyph: 'ܥ', name: 'E' },
  { glyph: 'ܦ', name: 'Pe' },
  { glyph: 'ܨ', name: 'Sadhe' },
  { glyph: 'ܩ', name: 'Qaph' },
  { glyph: 'ܪ', name: 'Rish' },
  { glyph: 'ܫ', name: 'Shin' },
  { glyph: 'ܬ', name: 'Taw' },
]

export const metadata = {
  title: 'The Syriac alphabet — AramiGo',
}

export default function AlphabetPage() {
  return (
    <div style={{ minHeight: '100vh', display: 'grid', gridTemplateRows: '1fr auto' }}>
      <main style={{ width: 'min(480px, 100%)', margin: '0 auto', padding: '1.25rem 1rem 2rem' }}>
        <h1 className="brand-font" style={{ fontSize: '1.7rem', color: 'var(--brand)', margin: 0 }}>
          The Syriac alphabet
        </h1>
        <p style={{ color: 'var(--muted)', marginTop: '0.35rem' }}>
          22 letters, written right to left. Vowels are marks added above and below —
          the lessons introduce them gradually.
        </p>

        <ul
          style={{
            listStyle: 'none',
            padding: 0,
            margin: '1.5rem 0 0',
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(96px, 1fr))',
            gap: '0.75rem',
          }}
        >
          {LETTERS.map(letter => (
            <li
              key={letter.name}
              style={{
                display: 'grid',
                gap: '0.25rem',
                justifyItems: 'center',
                padding: '0.85rem 0.5rem',
                borderRadius: 14,
                border: '1px solid var(--line)',
                background: 'var(--bg-elevated)',
              }}
            >
              <span className="syriac" style={{ fontSize: '2.1rem' }}>
                {letter.glyph}
              </span>
              <span style={{ fontSize: '0.82rem', color: 'var(--muted)', fontWeight: 700 }}>
                {letter.name}
              </span>
            </li>
          ))}
        </ul>
      </main>
      <BottomNav />
    </div>
  )
}
