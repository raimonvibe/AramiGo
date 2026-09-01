/**
 * Curated game portraits for destination screens (unit banners, rewards, About).
 * Never used as path-seal pictograms — see docs/COLOR.md.
 *
 * Skip game-image14 (Hebrew tablets) next to Syriac lessons.
 */
export const GAME_ART = {
  treasure: '/images/game-image2.png',
  unlock: '/images/game-image3.png',
  pause: '/images/game-image5.png',
  family: '/images/game-image7.png',
  script: '/images/game-image8.png',
  welcome: '/images/game-image1.png',
  harvest: '/images/game-image9.png',
  banner: '/images/game-image10.png',
  vigil: '/images/game-image6.png',
} as const

export type GameArtKey = keyof typeof GAME_ART

/**
 * Portraits in unit order, rotated the way `unitTheme` rotates gradients.
 *
 * The library is a fixed pack of nine and the curriculum is not, so pairing one
 * portrait to one unit forever was never going to hold — units 6 and 7 had
 * already fallen through to a generic banner. Rotating means a new unit needs no
 * art work at all, the same way it needs no colour work.
 *
 * The first five entries are the portraits units 1-5 were curated with, so the
 * rotation reproduces those pairings rather than reshuffling them.
 *
 * Alt text describes the *picture*, not the unit, because an entry is reused by
 * chapter 1 and chapter 10 alike. It is dropped entirely where these render
 * decoratively (see `ImagePlate`), which is every unit slot today.
 */
const ROTATION: { src: string; alt: string }[] = [
  { src: GAME_ART.harvest, alt: 'A learner figure holding grain' },
  { src: GAME_ART.family, alt: 'A craftsman figure with tools' },
  { src: GAME_ART.unlock, alt: 'A figure holding two keys' },
  { src: GAME_ART.welcome, alt: 'A welcoming figure in a white veil' },
  { src: GAME_ART.vigil, alt: 'A figure at prayer against a burst of light' },
  { src: GAME_ART.script, alt: 'A figure carrying a palm branch and a scroll' },
  { src: GAME_ART.banner, alt: 'A crowned figure with an open scroll' },
  { src: GAME_ART.treasure, alt: 'A winged figure with a trumpet and a sealed letter' },
  { src: GAME_ART.pause, alt: 'A figure with clasped hands and a rosary' },
]

/**
 * Nine portraits before one repeats — two chapters further apart than the seven
 * gradient families, so a portrait never returns while its colour is still on
 * screen.
 */
export function artForUnit(unitNumber: number): { src: string; alt: string } {
  return ROTATION[(unitNumber - 1) % ROTATION.length]
}
