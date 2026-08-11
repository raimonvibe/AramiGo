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
 */
const LETTERS: SyriacLetter[] = [
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
