import { PageShell, ImagePlate } from '@/shared/ui'
import { GAME_ART } from '@/shared/lib/gameArt'

export const metadata = {
  title: 'About AramiGo',
}

export default function AboutPage() {
  return (
    <PageShell>
      <h1 className="brand-font page-title">About AramiGo</h1>

      <div className="about-hero">
        <ImagePlate
          src={GAME_ART.script}
          alt="A figure holding a palm and a scroll — learning through writing"
          size="md"
        />
        <p style={{ margin: 0, color: 'var(--muted)', lineHeight: 1.55, maxWidth: '36rem' }}>
          Classical Syriac is the literary language of the Syriac churches. AramiGo keeps the path
          short and readable — script first, then meaning.
        </p>
      </div>

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
