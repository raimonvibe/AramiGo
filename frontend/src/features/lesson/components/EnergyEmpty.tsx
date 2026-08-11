'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { primaryButtonStyle } from './ExerciseCard'

function formatCountdown(totalSeconds: number): string {
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

/**
 * Running out of energy is a pause, not a wall — so say when it lifts.
 * Without a countdown this screen reads like the account is broken.
 */
export function EnergyEmpty({
  message,
  secondsUntilNextEnergy,
}: {
  message: string
  secondsUntilNextEnergy: number
}) {
  // Seeded once from the prop and counted down by the ticker. Callers reset it by
  // remounting (see the key in LessonPlayer) rather than syncing prop into state.
  const [remaining, setRemaining] = useState(() => Math.max(0, Math.round(secondsUntilNextEnergy)))

  useEffect(() => {
    const timer = setInterval(() => setRemaining(value => Math.max(0, value - 1)), 1000)
    return () => clearInterval(timer)
  }, [])

  return (
    <section
      style={{
        textAlign: 'center',
        marginTop: '2.5rem',
        display: 'grid',
        gap: '1.15rem',
        justifyItems: 'center',
      }}
    >
      <div
        style={{
          width: 120,
          height: 120,
          borderRadius: 28,
          background: 'linear-gradient(160deg, var(--energy), #8f4d66)',
          display: 'grid',
          placeItems: 'center',
          fontSize: '2.5rem',
          boxShadow: 'var(--shadow)',
        }}
        aria-hidden="true"
      >
        ⚡
      </div>
      <h2 className="brand-font" style={{ margin: 0, fontSize: '1.5rem', color: 'var(--brand)' }}>
        Out of energy
      </h2>
      <p style={{ margin: 0, color: 'var(--muted)', maxWidth: '30ch' }}>{message}</p>
      {remaining > 0 ? (
        <p style={{ margin: 0, fontWeight: 700 }}>
          Next point in <span style={{ color: 'var(--energy)' }}>{formatCountdown(remaining)}</span>
        </p>
      ) : (
        <p style={{ margin: 0, fontWeight: 700, color: 'var(--accent)' }}>
          A point is ready — head back and try again.
        </p>
      )}
      <Link href="/" style={{ ...primaryButtonStyle, display: 'block', textAlign: 'center' }}>
        BACK TO THE PATH
      </Link>
    </section>
  )
}
