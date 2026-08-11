import { BottomNav, SiteFooter } from '@/shared/ui'

export const metadata = {
  title: 'About AramiGo',
}

export default function AboutPage() {
  return (
    <div style={{ minHeight: '100vh', display: 'grid', gridTemplateRows: '1fr auto' }}>
      <main style={{ width: 'min(480px, 100%)', margin: '0 auto', padding: '1.25rem 1rem 2rem' }}>
        <h1 className="brand-font" style={{ fontSize: '1.7rem', color: 'var(--brand)', margin: 0 }}>
          About AramiGo
        </h1>

        <section style={{ display: 'grid', gap: '1rem', marginTop: '1.25rem', lineHeight: 1.6 }}>
          <p style={{ margin: 0 }}>
            AramiGo is a beginner&rsquo;s path into <strong>Classical Syriac Aramaic</strong> — the
            literary language of the Syriac churches, written in a right-to-left script of 22
            letters.
          </p>
          <p style={{ margin: 0, color: 'var(--muted)' }}>
            Lessons are short. You read the script, choose meanings, and build sentences from word
            chips. Getting something wrong costs a point of energy, which comes back on its own —
            there is no way to lock yourself out.
          </p>
          <p style={{ margin: 0, color: 'var(--muted)' }}>
            You can learn without an account. Signing in with Google saves your progress across
            devices and carries over everything you have already done.
          </p>
        </section>
      </main>
      <SiteFooter />
      <BottomNav />
    </div>
  )
}
