'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useCallback, useEffect, useRef, useState } from 'react'
import { AccountBar } from '@/features/auth'
import { AboutIcon, AccountIcon, AlphabetIcon, PathIcon } from './icons'
import { SocialIcon, SOCIAL_LINKS } from './SocialIcon'

/**
 * Destinations, each with the Syriac letter its name starts from — the seal
 * motif from the learning path, reused so the menu reads as part of the same
 * manuscript rather than a stock hamburger drawer.
 */
const ITEMS = [
  { href: '/', label: 'Path', letter: 'ܐ', Icon: PathIcon },
  { href: '/alphabet', label: 'Alphabet', letter: 'ܒ', Icon: AlphabetIcon },
  { href: '/account', label: 'Account', letter: 'ܓ', Icon: AccountIcon },
  { href: '/about', label: 'About', letter: 'ܕ', Icon: AboutIcon },
]

export function AppMenu() {
  const pathname = usePathname()
  // Which page the menu was opened from, rather than a plain boolean: a menu
  // that survived the navigation it just triggered would cover the page the
  // learner asked for, and this closes it on back/forward too.
  const [openedAt, setOpenedAt] = useState<string | null>(null)
  const open = openedAt !== null && openedAt === pathname
  const panelRef = useRef<HTMLDivElement | null>(null)
  const triggerRef = useRef<HTMLButtonElement | null>(null)

  const close = useCallback(() => setOpenedAt(null), [])

  useEffect(() => {
    if (!open) return

    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        close()
        triggerRef.current?.focus()
      }
    }
    const onPointer = (event: PointerEvent) => {
      const target = event.target as Node
      if (panelRef.current?.contains(target) || triggerRef.current?.contains(target)) return
      close()
    }

    document.addEventListener('keydown', onKey)
    document.addEventListener('pointerdown', onPointer)
    return () => {
      document.removeEventListener('keydown', onKey)
      document.removeEventListener('pointerdown', onPointer)
    }
  }, [open, close])

  return (
    <div className="app-menu">
      {open && (
        <div className="app-menu-panel" ref={panelRef} role="dialog" aria-label="Menu">
          <div className="app-menu-rule" aria-hidden="true" />

          <nav aria-label="All pages">
            <ul className="app-menu-list">
              {ITEMS.map(item => {
                const active = pathname === item.href
                const Icon = item.Icon
                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      aria-current={active ? 'page' : undefined}
                      className="app-menu-link"
                      onClick={close}
                    >
                      <span className="app-menu-seal" aria-hidden="true">
                        <span className="syriac">{item.letter}</span>
                      </span>
                      <span className="app-menu-label">{item.label}</span>
                      <Icon size={20} />
                    </Link>
                  </li>
                )
              })}
            </ul>
          </nav>

          <div className="app-menu-rule" aria-hidden="true" />

          <AccountBar />

          <div className="app-menu-social">
            <span className="app-menu-madeby">
              Made by{' '}
              <a
                href="https://www.raimonvibe.eu/"
                target="_blank"
                rel="noopener noreferrer me"
                style={{ color: 'var(--brand)', fontWeight: 700 }}
              >
                raimonvibe
              </a>
            </span>
            <nav aria-label="raimonvibe on the web" className="site-footer-links">
              {SOCIAL_LINKS.map(link => (
                <SocialIcon key={link.id} link={link} size={22} />
              ))}
            </nav>
          </div>
        </div>
      )}

      <button
        type="button"
        ref={triggerRef}
        className="app-menu-trigger"
        aria-expanded={open}
        aria-haspopup="dialog"
        onClick={() => setOpenedAt(open ? null : pathname)}
      >
        <span className="app-menu-trigger-seal" aria-hidden="true">
          <span className="syriac">ܐ</span>
        </span>
        <span>{open ? 'Close' : 'Menu'}</span>
      </button>
    </div>
  )
}
