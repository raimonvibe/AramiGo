import type { MetadataRoute } from 'next'

/** Homescreen / install metadata — uses the AramiGo app icon. */
export default function manifest(): MetadataRoute.Manifest {
  return {
    name: 'AramiGo',
    short_name: 'AramiGo',
    description: 'A beginner-friendly path into Classical Syriac Aramaic.',
    start_url: '/',
    display: 'standalone',
    background_color: '#121a1f',
    theme_color: '#121a1f',
    icons: [
      {
        src: '/images/app-icon.png',
        sizes: '495x504',
        type: 'image/png',
        purpose: 'any',
      },
      {
        src: '/apple-touch-icon.png',
        sizes: '495x504',
        type: 'image/png',
        purpose: 'any',
      },
    ],
  }
}
