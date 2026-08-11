import type { CSSProperties, ReactNode } from 'react'
import { BRAND_PATHS } from './brand-paths'
import styles from './SocialIcon.module.css'

export type SocialId =
  | 'website'
  | 'x'
  | 'youtube'
  | 'tiktok'
  | 'instagram'
  | 'medium'
  | 'github'
  | 'linkedin'
  | 'facebook'

export interface SocialLink {
  id: SocialId
  label: string
  url: string
  /** Brand colour used in light mode. */
  colour: string
  /** Lighter stand-in for near-black marks on dark parchment. */
  colourDark?: string
  fill?: 'solid' | 'gradient' | 'chromatic'
}

/**
 * Maker links from raimonvibe/digital-marketing SocialIcon — same order and URLs.
 */
export const SOCIAL_LINKS: SocialLink[] = [
  {
    id: 'website',
    label: 'raimonvibe.eu',
    url: 'https://www.raimonvibe.eu/',
    colour: '#4b483f',
    colourDark: '#bcb5a4',
  },
  {
    id: 'x',
    label: 'X',
    url: 'https://x.com/raimonvibe/',
    colour: '#000000',
    colourDark: '#efeade',
  },
  {
    id: 'youtube',
    label: 'YouTube',
    url: 'https://www.youtube.com/channel/UCDGDNuYb2b2Ets9CYCNVbuA/videos/',
    colour: '#ff0000',
  },
  {
    id: 'tiktok',
    label: 'TikTok',
    url: 'https://www.tiktok.com/@raimonvibe/',
    colour: '#000000',
    colourDark: '#efeade',
    fill: 'chromatic',
  },
  {
    id: 'instagram',
    label: 'Instagram',
    url: 'https://www.instagram.com/raimonvibe/',
    colour: '#e1306c',
    fill: 'gradient',
  },
  {
    id: 'medium',
    label: 'Medium',
    url: 'https://medium.com/@raimonvibe/',
    colour: '#000000',
    colourDark: '#efeade',
  },
  {
    id: 'github',
    label: 'GitHub',
    url: 'https://github.com/raimonvibe/',
    colour: '#181717',
    colourDark: '#efeade',
  },
  {
    id: 'linkedin',
    label: 'LinkedIn',
    url: 'https://www.linkedin.com/in/raimonvibe/',
    colour: '#0a66c2',
  },
  {
    id: 'facebook',
    label: 'Facebook',
    url: 'https://www.facebook.com/profile.php?id=61563450007849',
    colour: '#1877f2',
  },
]

const LOCAL_PATHS: Partial<Record<SocialId, ReactNode>> = {
  linkedin: (
    <>
      <circle cx="2.6" cy="2.6" r="2.6" />
      <path d="M.2 7.4h4.8V24H.2z" />
      <path d="M8.4 7.4h4.6v2.3h.07c.64-1.2 2.2-2.47 4.53-2.47 4.84 0 5.74 3.1 5.74 7.2V24h-4.8v-7.86c0-1.88-.04-4.3-2.66-4.3-2.66 0-3.06 2.03-3.06 4.16V24H8.4z" />
    </>
  ),
  website: (
    <g fill="none" stroke="currentColor" strokeWidth="1.7">
      <circle cx="12" cy="12" r="10.2" />
      <ellipse cx="12" cy="12" rx="4.4" ry="10.2" />
      <path d="M2.2 8.6h19.6M2.2 15.4h19.6M12 1.8v20.4" />
    </g>
  ),
}

/**
 * One social mark. Solid brand colour, Instagram gradient, or TikTok chromatic stack.
 * AramiGo is always dark parchment, so colourDark is preferred when present.
 */
export function SocialIcon({ link, size = 22 }: { link: SocialLink; size?: number }) {
  const gradientId = `ig-gradient-${link.id}`
  const local = LOCAL_PATHS[link.id]
  const path = BRAND_PATHS[link.id]
  const style = {
    '--brand': link.colourDark ?? link.colour,
    '--brand-dark': link.colourDark ?? link.colour,
  } as CSSProperties

  return (
    <a
      className={styles.link}
      href={link.url}
      target="_blank"
      rel="noopener noreferrer me"
      title={link.label}
      style={style}
      data-fill={link.fill ?? 'solid'}
    >
      <span className={styles.icon}>
        {link.fill === 'chromatic' && path ? (
          <svg viewBox="0 0 24 24" width={size} height={size} aria-hidden="true" focusable="false">
            <path d={path} fill="#25f4ee" transform="translate(-1.1 -0.9)" />
            <path d={path} fill="#fe2c55" transform="translate(1.1 0.9)" />
            <path d={path} className={styles.brandFill} />
          </svg>
        ) : (
          <svg viewBox="0 0 24 24" width={size} height={size} aria-hidden="true" focusable="false">
            {link.fill === 'gradient' && (
              <defs>
                <linearGradient id={gradientId} x1="0" y1="1" x2="1" y2="0">
                  <stop offset="0%" stopColor="#ffdc80" />
                  <stop offset="18%" stopColor="#fcaf45" />
                  <stop offset="34%" stopColor="#f77737" />
                  <stop offset="48%" stopColor="#f56040" />
                  <stop offset="60%" stopColor="#fd1d1d" />
                  <stop offset="72%" stopColor="#e1306c" />
                  <stop offset="83%" stopColor="#c13584" />
                  <stop offset="92%" stopColor="#833ab4" />
                  <stop offset="100%" stopColor="#405de6" />
                </linearGradient>
              </defs>
            )}
            <g
              fill={link.fill === 'gradient' ? `url(#${gradientId})` : undefined}
              className={link.fill === 'gradient' ? undefined : styles.brandFill}
            >
              {local ?? <path d={path} />}
            </g>
          </svg>
        )}
      </span>
      <span className="visually-hidden">{link.label}</span>
    </a>
  )
}
