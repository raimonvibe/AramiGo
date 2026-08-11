import { PageShell } from '@/shared/ui'

export const metadata = {
  title: 'About AramiGo',
}

export default function AboutPage() {
  return (
    <PageShell>
      <h1 className="brand-font page-title">About AramiGo</h1>

      <section className="about-copy">
        <p style={{ margin: 0 }}>
          AramiGo is a beginner&rsquo;s path into <strong>Classical Syriac Aramaic</strong> — the
          literary language of the Syriac churches, written in a right-to-left script of 22 letters.
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
    </PageShell>
  )
}
