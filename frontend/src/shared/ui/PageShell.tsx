import type { ReactNode } from 'react'
import { BottomNav } from './BottomNav'
import { SiteFooter } from './SiteFooter'

/**
 * Shared chrome for Path / Alphabet / Account / About.
 * Width and padding come from CSS variables so tablet/desktop can breathe
 * without each page inventing its own max-width.
 */
export function PageShell({
  children,
  mainClassName,
}: {
  children: ReactNode
  /** Extra class on main (e.g. account-main for a two-column body). */
  mainClassName?: string
}) {
  return (
    <div className="page-shell">
      <main className={['page-main', mainClassName].filter(Boolean).join(' ')}>{children}</main>
      <SiteFooter />
      <BottomNav />
    </div>
  )
}
