import { PageShell } from '@/shared/ui'

export const metadata = {
  title: 'Support · Listen on your device',
}

const PLATFORMS = [
  {
    name: 'iPhone / iPad',
    summary: 'Hebrew voices usually come with iOS.',
    steps: [
      'Open Settings → Accessibility → Spoken Content (or Settings → General → Language & Region).',
      'Under Voices, open Hebrew and download a voice if one is not already installed.',
      'Return to AramiGo in Safari (or your browser) and reload the page.',
    ],
  },
  {
    name: 'Android',
    summary: 'Depends on the device’s text-to-speech engine (often Google).',
    steps: [
      'Open Settings → System → Languages & input → Text-to-speech output (wording varies by phone).',
      'Open the preferred engine’s settings and install a Hebrew voice / language data.',
      'Reload AramiGo in Chrome. If listen still falls back, try downloading Hebrew under Settings → System → Languages first.',
    ],
  },
  {
    name: 'Windows 11',
    summary: 'Hebrew speech is optional — install it once in Windows Settings.',
    steps: [
      'Open Settings → Time & language → Language & region → Add a language → Hebrew.',
      'During install, enable Text-to-speech (Speech).',
      'Then open Settings → Time & language → Speech → Manage voices / Add voices and install a Hebrew voice (for example Hila or Avri).',
      'Fully quit the browser, reopen it, and reload AramiGo. Microsoft Edge usually picks up new Windows voices most reliably.',
    ],
  },
  {
    name: 'macOS',
    summary: 'Add a Hebrew system voice, then reload the browser.',
    steps: [
      'Open System Settings → Accessibility → Spoken Content (on older macOS: System Preferences → Accessibility → Speech).',
      'Open System Voices / Manage Voices and download a Hebrew voice.',
      'Reload AramiGo in Safari or Chrome.',
    ],
  },
  {
    name: 'Linux',
    summary: 'Many distros ship with no browser speech voices at all.',
    steps: [
      'Install a speech dispatcher / TTS package your desktop supports (for example speech-dispatcher plus a Hebrew-capable engine, if available).',
      'Prefer Firefox or Chromium builds that expose system voices to the Web Speech API.',
      'If no voice appears, listen prompts stay silent and AramiGo shows the written script instead — that is expected on many Linux setups.',
    ],
  },
] as const

export default function SupportPage() {
  return (
    <PageShell>
      <h1 className="brand-font page-title">Support</h1>

      <p style={{ margin: '0 0 1.5rem', color: 'var(--muted)', lineHeight: 1.55, maxWidth: '40rem' }}>
        Classical Syriac has no built-in speech engine. Listen mode uses a{' '}
        <strong style={{ color: 'var(--text)' }}>Hebrew</strong> system voice as a closer Semitic
        stand-in. Whether that works depends on voices installed on your device — not on an AramiGo
        setting.
      </p>

      <section className="support-platforms" aria-label="Listen setup by platform">
        {PLATFORMS.map(platform => (
          <article key={platform.name} className="support-platform">
            <h2 className="brand-font support-platform-title">{platform.name}</h2>
            <p className="support-platform-summary">{platform.summary}</p>
            <ol className="support-platform-steps">
              {platform.steps.map(step => (
                <li key={step}>{step}</li>
              ))}
            </ol>
          </article>
        ))}
      </section>

      <p style={{ margin: '1.75rem 0 0', color: 'var(--muted)', fontSize: '0.9rem', lineHeight: 1.5, maxWidth: '40rem' }}>
        After installing a Hebrew voice, you should see a note that listen mode is using Hebrew. If
        you still hear an English (or other) voice, the browser has not picked up the new pack yet —
        restart the browser once more.
      </p>
    </PageShell>
  )
}
