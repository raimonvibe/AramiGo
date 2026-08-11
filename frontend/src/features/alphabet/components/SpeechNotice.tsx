'use client'

import { useEffect, useState } from 'react'
import { onVoicesChanged, speechNotice, type SpeechAvailability } from '@/shared/lib/speech'

/**
 * The one place the page owns up to what the voice actually is. Rendered once
 * for the whole page — every play control on it shares the same caveat.
 */
function noticeFor(availability: SpeechAvailability): string | null {
  if (availability === 'unsupported') {
    return 'This browser has no speech engine, so nothing on this page can be read aloud.'
  }
  if (availability === 'no-voice') {
    return 'No system voice is installed yet, so nothing on this page can be read aloud.'
  }
  return speechNotice()
}

export function SpeechNotice() {
  // Held in state rather than read during render: the server has no speech
  // engine to ask, so computing this inline would not survive hydration.
  const [text, setText] = useState<string | null>(null)

  useEffect(() => onVoicesChanged(availability => setText(noticeFor(availability))), [])

  if (!text) return null

  return (
    <p
      style={{
        color: 'var(--muted)',
        fontSize: '0.8rem',
        marginTop: '1.5rem',
        lineHeight: 1.45,
      }}
    >
      {text}
    </p>
  )
}
