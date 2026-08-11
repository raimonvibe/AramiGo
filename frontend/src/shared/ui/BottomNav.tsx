'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { AboutIcon, AccountIcon, AlphabetIcon, PathIcon } from './icons'

const items = [
  { href: '/', label: 'Path', Icon: PathIcon },
  { href: '/alphabet', label: 'Alphabet', Icon: AlphabetIcon },
  { href: '/account', label: 'Account', Icon: AccountIcon },
  { href: '/about', label: 'About', Icon: AboutIcon },
]

export function BottomNav() {
  const pathname = usePathname()

  return (
    <nav
      style={{
        position: 'sticky',
        bottom: 0,
        display: 'grid',
        gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
        gap: '0.35rem',
        padding: '0.75rem 0.65rem calc(0.75rem + env(safe-area-inset-bottom))',
        background: 'rgba(18, 26, 31, 0.92)',
        borderTop: '1px solid var(--line)',
        backdropFilter: 'blur(10px)',
      }}
    >
      {items.map(item => {
        const active = pathname === item.href
        const Icon = item.Icon
        return (
          <Link
            key={item.href}
            href={item.href}
            aria-current={active ? 'page' : undefined}
            style={{
              display: 'grid',
              placeItems: 'center',
              gap: '0.15rem',
              padding: '0.5rem 0.25rem',
              borderRadius: '14px',
              border: active ? '2px solid var(--brand)' : '2px solid transparent',
              color: active ? 'var(--brand)' : 'var(--muted)',
              fontSize: '0.72rem',
              fontWeight: 700,
              textAlign: 'center',
              minWidth: 0,
            }}
          >
            <Icon size={20} />
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '100%' }}>
              {item.label}
            </span>
          </Link>
        )
      })}
    </nav>
  )
}
