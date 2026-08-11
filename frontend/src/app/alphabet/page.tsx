import { PageShell, ImagePlate } from '@/shared/ui'
import { GAME_ART } from '@/shared/lib/gameArt'
import {
  AlphabetGrid,
  NumbersSection,
  SpeechNotice,
  type SyriacLetter,
  type SyriacNumber,
} from '@/features/alphabet'

/**
 * The 22 letters of the Syriac abjad in traditional order.
 *
 * The names are read aloud as written here. East and West Syriac disagree on
 * several of them, so these follow the West Syriac forms the curriculum already
 * teaches — the page and the lessons must never pronounce a letter differently.
 *
 * `value` is the letter's numeral value: Syriac writes numbers with letters,
 * ones through 9, then tens, then hundreds.
 */
const LETTERS: SyriacLetter[] = [
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

      <AlphabetGrid letters={LETTERS} />

      <NumbersSection numbers={NUMBERS} />

      <SpeechNotice />
    </PageShell>
  )
}
