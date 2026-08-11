import type { CSSProperties } from 'react'

type PlateSize = 'sm' | 'md' | 'lg'

const SIZE: Record<PlateSize, CSSProperties> = {
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
