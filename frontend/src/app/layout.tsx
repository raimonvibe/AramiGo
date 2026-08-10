import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'AramiGo — Learn Aramaic',
  description: 'A beginner-friendly path into Classical Syriac Aramaic.',
}

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
