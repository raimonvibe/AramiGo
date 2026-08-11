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
    <nav className="bottom-nav" aria-label="Primary">
      <div className="bottom-nav-inner">
        {items.map(item => {
          const active = pathname === item.href
          const Icon = item.Icon
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? 'page' : undefined}
              className="bottom-nav-link"
            >
              <Icon size={22} />
              <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '100%' }}>
                {item.label}
              </span>
            </Link>
          )
        })}
      </div>
    </nav>
  )
}
