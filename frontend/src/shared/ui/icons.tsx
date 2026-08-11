import type { CSSProperties, ReactNode, SVGProps } from 'react'

type IconProps = SVGProps<SVGSVGElement> & { size?: number }

function BaseIcon({ size = 24, children, ...props }: IconProps & { children: ReactNode }) {
  return (
    <svg
      viewBox="0 0 24 24"
      width={size}
      height={size}
      fill="none"
      stroke="currentColor"
      strokeWidth={1.7}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
      {...props}
    >
      {children}
    </svg>
  )
}

/** Vertical ruled line with a manuscript seal — learning path. */
export function PathIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <line x1="8" y1="2" x2="8" y2="22" />
      <circle cx="8" cy="8" r="3.2" fill="currentColor" stroke="none" opacity={0.9} />
      <circle cx="8" cy="8" r="1.35" fill="var(--bg, #121a1f)" stroke="none" />
      <path d="M14 6.5h6M14 10h4.5M14 13.5h5" opacity={0.75} />
    </BaseIcon>
  )
}

/** Stylized Syriac Alaph form — alphabet. */
export function AlphabetIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <path d="M7 20c1.2-5.5 2.2-10.2 2.6-14.2.15-1.5 2.5-1.55 2.7.05.45 3.7 1.2 7.8 2.3 12.15" />
      <path d="M6.2 11.2c3.6-1.1 7.4-1.15 11.6.15" />
      <path d="M9.2 7.4c2.4.55 4.7.7 7 .35" opacity={0.7} />
    </BaseIcon>
  )
}

/** Wax seal silhouette — account. */
export function AccountIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <circle cx="12" cy="12" r="8.4" />
      <circle cx="12" cy="10.2" r="2.6" />
      <path d="M7.4 17.2c1.2-2.1 2.9-3.1 4.6-3.1s3.4 1 4.6 3.1" />
      <path d="M12 3.6v1.4M12 19v1.4M3.6 12h1.4M19 12h1.4" opacity={0.55} />
    </BaseIcon>
  )
}

/** Open folio / bifolium — about. */
export function AboutIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <path d="M12 5.2c-2.4-1.4-5.2-1.7-7.6-1.2v13.4c2.4-.5 5.2-.2 7.6 1.2 2.4-1.4 5.2-1.7 7.6-1.2V4c-2.4-.5-5.2-.2-7.6 1.2z" />
      <path d="M12 5.2v13.4" />
    </BaseIcon>
  )
}

/** Soft voice waves — listen / support. */
export function SupportIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <path d="M5 10.5v3" />
      <path d="M8.2 8.2v7.6" />
      <path d="M11.4 6v12" />
      <path d="M14.8 8.5c1.35 1.1 1.35 5.9 0 7" opacity={0.85} />
      <path d="M17.6 6.4c2.35 2.1 2.35 9.1 0 11.2" opacity={0.55} />
    </BaseIcon>
  )
}

/** Oil lamp — energy. */
export function EnergyIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <path d="M7.5 14.5c0-2.6 1.7-4.2 4.5-5.8 2.8 1.6 4.5 3.2 4.5 5.8 0 2.4-1.9 3.8-4.5 3.8s-4.5-1.4-4.5-3.8z" />
      <path d="M12 5.2c.7 1.2.9 2.2.35 3.2-.7-.35-1.25-1.15-1.35-2.2.55-.35 1-.7 1-.1z" fill="currentColor" stroke="none" opacity={0.85} />
      <path d="M6.2 14.8H5.1c-.7 0-1.1.45-1.1 1.05S4.4 17 5.1 17h13.8c.7 0 1.1-.5 1.1-1.15s-.4-1.05-1.1-1.05h-1.2" />
    </BaseIcon>
  )
}

/** Gold lozenge / tessera — gems. */
export function GemIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <path d="M12 3.2 19.2 12 12 20.8 4.8 12z" />
      <path d="M12 3.2 14.8 12 12 20.8 9.2 12z" opacity={0.55} />
      <path d="M6.4 9.2h11.2M6.4 14.8h11.2" opacity={0.45} />
    </BaseIcon>
  )
}

/** Continuous ink stroke / day mark — streak. */
export function StreakIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <path d="M4.2 16.8c2.4-1.1 3.8-3.4 4.1-6.1.2-1.9 1.4-3.5 3.2-4.2" />
      <path d="M11.2 6.2c2.3-.4 4.4.6 5.6 2.5 1.4 2.2 1.7 4.8.4 7.1" />
      <path d="M8.6 18.4c1.6.4 3.3.4 4.9 0" opacity={0.65} />
      <circle cx="17.6" cy="16.8" r="1.35" fill="currentColor" stroke="none" />
    </BaseIcon>
  )
}

/** Reed pen tip in a roundel — play audio at normal rate. */
export function PlayIcon({ size = 26, ...props }: IconProps) {
  return (
    <BaseIcon size={size} {...props}>
      <circle cx="12" cy="12" r="9" />
      <path d="M10.2 7.4 16.4 12l-6.2 4.6V7.4z" fill="currentColor" stroke="none" />
    </BaseIcon>
  )
}

/** Double vertical caesura — play slowly. */
export function SlowPlayIcon({ size = 22, ...props }: IconProps) {
  return (
    <BaseIcon size={size} strokeWidth={2.2} {...props}>
      <line x1="9" y1="6" x2="9" y2="18" />
      <line x1="15" y1="6" x2="15" y2="18" />
      <path d="M6.5 12h2M15.5 12h2" opacity={0.45} />
    </BaseIcon>
  )
}

export function IconLabel({
  icon,
  children,
  color,
  gap = '0.35rem',
}: {
  icon: ReactNode
  children: ReactNode
  color?: string
  gap?: string
}) {
  const style: CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    gap,
    color,
  }
  return <span style={style}>{icon}{children}</span>
}
