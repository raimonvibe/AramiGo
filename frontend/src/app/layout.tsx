import type { Metadata } from 'next'
import { Literata, Noto_Sans_Syriac, Source_Sans_3 } from 'next/font/google'
import './globals.css'

// Self-hosted by next/font: no render-blocking request to fonts.googleapis.com
// and no third-party call from the learner's browser.
const body = Source_Sans_3({
  subsets: ['latin'],
  weight: ['400', '600', '700'],
  variable: '--font-body',
  display: 'swap',
})

const brand = Literata({
  subsets: ['latin'],
  weight: ['500', '700'],
  variable: '--font-brand',
  display: 'swap',
})

const syriac = Noto_Sans_Syriac({
  subsets: ['syriac'],
  weight: ['400', '700'],
  variable: '--font-syriac',
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'AramiGo — Learn Aramaic',
  description: 'A beginner-friendly path into Classical Syriac Aramaic.',
}

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html
      lang="en"
      className={`${body.variable} ${brand.variable} ${syriac.variable}`}
    >
      <body>{children}</body>
    </html>
  )
}
