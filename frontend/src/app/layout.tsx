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

const siteDescription =
  'A beginner-friendly path into Classical Syriac Aramaic.'

/** Absolute base for Open Graph / Twitter image URLs when sharing the site. */
const siteUrl =
  process.env.NEXT_PUBLIC_SITE_URL?.replace(/\/$/, '') ??
  'https://arami-go-gv7b.vercel.app'

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: {
    default: 'AramiGo — Learn Aramaic',
    template: '%s',
  },
  description: siteDescription,
  applicationName: 'AramiGo',
  // Icons + share images come from app/ file conventions:
  // favicon.ico, apple-icon.png, opengraph-image.png, twitter-image.png
  appleWebApp: {
    capable: true,
    title: 'AramiGo',
    statusBarStyle: 'black-translucent',
  },
  openGraph: {
    type: 'website',
    locale: 'en_US',
    siteName: 'AramiGo',
    title: 'AramiGo — Learn Aramaic',
    description: siteDescription,
  },
  twitter: {
    card: 'summary_large_image',
    title: 'AramiGo — Learn Aramaic',
    description: siteDescription,
  },
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
