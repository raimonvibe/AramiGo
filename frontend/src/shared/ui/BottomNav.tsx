'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

const items = [
  { href: '/', label: 'Path', icon: '⌂' },
  { href: '/#practice', label: 'Practice', icon: '✦' },
  { href: '/#about', label: 'About', icon: '◌' },
]

export function BottomNav() {
  const pathname = usePathname()

  return (
    <nav
      style={{
        position: 'sticky',
        bottom: 0,
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '0.5rem',
        padding: '0.75rem 1rem calc(0.75rem + env(safe-area-inset-bottom))',
        background: 'rgba(18, 26, 31, 0.92)',
        borderTop: '1px solid var(--line)',
        backdropFilter: 'blur(10px)',
      }}
    >
      {items.map(item => {
        const active = pathname === item.href
        return (
          <Link
            key={item.href}
            href={item.href}
            style={{
              display: 'grid',
              placeItems: 'center',
              gap: '0.15rem',
              padding: '0.55rem',
              borderRadius: '14px',
              border: active ? '2px solid var(--brand)' : '2px solid transparent',
              color: active ? 'var(--brand)' : 'var(--muted)',
              fontSize: '0.8rem',
              fontWeight: 700,
            }}
          >
            <span style={{ fontSize: '1.15rem' }}>{item.icon}</span>
            {item.label}
          </Link>
        )
      })}
    </nav>
  )
}
