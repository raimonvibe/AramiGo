import { PageShell, ImagePlate } from '@/shared/ui'
import { GAME_ART } from '@/shared/lib/gameArt'

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
    <PageShell>
      <h1 className="brand-font page-title">The Syriac alphabet</h1>
      <div className="alphabet-hero">
        <ImagePlate
          src={GAME_ART.script}
          alt="A guide with a scroll — the script comes first"
          size="sm"
        />
        <p className="page-lede" style={{ margin: 0 }}>
          22 letters, written right to left. Vowels are marks added above and below — the lessons
          introduce them gradually.
        </p>
      </div>

      <ul className="alphabet-grid">
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
    </PageShell>
  )
}
