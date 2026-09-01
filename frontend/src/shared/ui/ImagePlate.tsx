import type { CSSProperties } from 'react'

type PlateSize = 'xs' | 'sm' | 'md' | 'lg'

const SIZE: Record<PlateSize, CSSProperties> = {
  /* Illumination beside a line of text — the bookmark, contents rows. */
  xs: { width: 'clamp(3.25rem, 12vw, 4.5rem)' },
  sm: { width: 'clamp(5.5rem, 22vw, 7.5rem)' },
  md: { width: 'clamp(7.5rem, 28vw, 11rem)' },
  lg: { width: 'clamp(9rem, 32vw, 14rem)' },
}

/**
 * Square game portrait that scales with the viewport.
 * Decorative when alt is empty; otherwise provide a short meaningful alt.
 */
export function ImagePlate({
  src,
  alt,
  size = 'md',
  decorative = false,
}: {
  src: string
  alt: string
  size?: PlateSize
  /** When true, hides from assistive tech (paired visible text already explains). */
  decorative?: boolean
}) {
  return (
    // Decorative portraits — Next Image not required for static public assets.
    // These ship at a fixed size from /public and are mostly aria-hidden, so the
    // optimiser would add a round trip and a dependency for no benefit.
    // eslint-disable-next-line @next/next/no-img-element
    <img
      src={src}
      alt={decorative ? '' : alt}
      aria-hidden={decorative || undefined}
      width={495}
      height={504}
      loading="lazy"
      decoding="async"
      className="image-plate"
      style={SIZE[size]}
    />
  )
}
