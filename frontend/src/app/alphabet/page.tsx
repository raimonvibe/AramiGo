import { PageShell, ImagePlate } from '@/shared/ui'
import { GAME_ART } from '@/shared/lib/gameArt'
import { NumbersSection, type SyriacNumber } from '@/features/alphabet'

/**
 * The 22 letters of the Syriac abjad in traditional order.
 *
 * Names only — pronunciation is deliberately left to the lessons, because East
 * and West Syriac differ on several letters and picking one here would quietly
 * teach a dialect the curriculum hasn't chosen yet.
 *
 * `value` is the letter's numeral value: Syriac writes numbers with letters,
 * ones through 9, then tens, then hundreds.
 */
const LETTERS: { glyph: string; name: string; value: number }[] = [
  { glyph: 'ܐ', name: 'Alaph', value: 1 },
  { glyph: 'ܒ', name: 'Beth', value: 2 },
  { glyph: 'ܓ', name: 'Gamal', value: 3 },
  { glyph: 'ܕ', name: 'Dalath', value: 4 },
  { glyph: 'ܗ', name: 'He', value: 5 },
  { glyph: 'ܘ', name: 'Waw', value: 6 },
  { glyph: 'ܙ', name: 'Zayn', value: 7 },
  { glyph: 'ܚ', name: 'Heth', value: 8 },
  { glyph: 'ܛ', name: 'Teth', value: 9 },
  { glyph: 'ܝ', name: 'Yudh', value: 10 },
  { glyph: 'ܟ', name: 'Kaph', value: 20 },
  { glyph: 'ܠ', name: 'Lamadh', value: 30 },
  { glyph: 'ܡ', name: 'Mim', value: 40 },
  { glyph: 'ܢ', name: 'Nun', value: 50 },
  { glyph: 'ܣ', name: 'Semkath', value: 60 },
  { glyph: 'ܥ', name: 'E', value: 70 },
  { glyph: 'ܦ', name: 'Pe', value: 80 },
  { glyph: 'ܨ', name: 'Sadhe', value: 90 },
  { glyph: 'ܩ', name: 'Qaph', value: 100 },
  { glyph: 'ܪ', name: 'Rish', value: 200 },
  { glyph: 'ܫ', name: 'Shin', value: 300 },
  { glyph: 'ܬ', name: 'Taw', value: 400 },
]

/**
 * One to ten. Script and romanization follow the curriculum's West Syriac
 * forms (`had`, `trein`, `tloto` are already taught in section 1, unit 2), so
 * the page and the lessons never disagree on how a number sounds.
 */
const NUMBERS: SyriacNumber[] = [
  { value: 1, numeral: 'ܐ', word: 'ܚܕ', transliteration: 'had' },
  { value: 2, numeral: 'ܒ', word: 'ܬܪܝܢ', transliteration: 'trein' },
  { value: 3, numeral: 'ܓ', word: 'ܬܠܬܐ', transliteration: 'tloto' },
  { value: 4, numeral: 'ܕ', word: 'ܐܪܒܥܐ', transliteration: 'arbo' },
  { value: 5, numeral: 'ܗ', word: 'ܚܡܫܐ', transliteration: 'hamsho' },
  { value: 6, numeral: 'ܘ', word: 'ܫܬܐ', transliteration: 'eshto' },
  { value: 7, numeral: 'ܙ', word: 'ܫܒܥܐ', transliteration: 'shabo' },
  { value: 8, numeral: 'ܚ', word: 'ܬܡܢܝܐ', transliteration: 'tmonyo' },
  { value: 9, numeral: 'ܛ', word: 'ܬܫܥܐ', transliteration: 'tesho' },
  { value: 10, numeral: 'ܝ', word: 'ܥܣܪܐ', transliteration: 'esro' },
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
            <span
              style={{ fontSize: '0.72rem', color: 'var(--muted)', opacity: 0.75 }}
              title={`${letter.name} stands for ${letter.value} when used as a numeral`}
            >
              = {letter.value}
            </span>
          </li>
        ))}
      </ul>

      <NumbersSection numbers={NUMBERS} />
    </PageShell>
  )
}
