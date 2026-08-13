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
} as const

export type GameArtKey = keyof typeof GAME_ART

export function artForUnit(
  sectionNumber: number,
  unitNumber: number,
): { src: string; alt: string } {
  if (sectionNumber === 1 && unitNumber === 1) {
    return {
      src: GAME_ART.harvest,
      alt: 'A learner figure with grain — everyday phrases and food words',
    }
  }
  if (sectionNumber === 1 && unitNumber === 2) {
    return {
      src: GAME_ART.family,
      alt: 'A craftsman figure — numbers and family words',
    }
  }
  if (sectionNumber === 1 && unitNumber === 3) {
    return {
      src: GAME_ART.unlock,
      alt: 'A figure holding two keys — arriving at a house',
    }
  }
  if (sectionNumber === 1 && unitNumber === 4) {
    return {
      src: GAME_ART.welcome,
      alt: 'A welcoming figure in a white veil — a meal set at the table',
    }
  }
  return {
    src: GAME_ART.banner,
    alt: 'A guide with an open scroll',
  }
}
