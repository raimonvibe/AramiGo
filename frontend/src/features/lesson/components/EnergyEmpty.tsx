'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { ImagePlate } from '@/shared/ui'
import { GAME_ART } from '@/shared/lib/gameArt'
import {
  ApiError,
  GEMS_PER_REFILL,
  refillEnergy,
  type LearnerStats,
} from '@/shared/lib/api'
import { primaryButtonStyle } from './lessonStyles'

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
  gems,
  onRefilled,
}: {
  message: string
  secondsUntilNextEnergy: number
  /** Omit to hide the refill offer entirely — a caller that does not know cannot offer. */
  gems?: number
  onRefilled?: (stats: LearnerStats) => void
}) {
  // Seeded once from the prop and counted down by the ticker. Callers reset it by
  // remounting (see the key in LessonPlayer) rather than syncing prop into state.
  const [remaining, setRemaining] = useState(() => Math.max(0, Math.round(secondsUntilNextEnergy)))
  const [spending, setSpending] = useState(false)
  const [refillError, setRefillError] = useState<string | null>(null)

  useEffect(() => {
    const timer = setInterval(() => setRemaining(value => Math.max(0, value - 1)), 1000)
    return () => clearInterval(timer)
  }, [])

  const canAfford = gems !== undefined && gems >= GEMS_PER_REFILL

  async function buyRefill() {
    if (spending) return
    setSpending(true)
    setRefillError(null)
    try {
      const result = await refillEnergy()
      onRefilled?.(result.stats)
    } catch (err) {
      setRefillError(
        err instanceof ApiError ? err.message : 'Could not spend your gems just now.',
      )
      setSpending(false)
    }
  }

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
      <div className="stage-art">
        <ImagePlate
          src={GAME_ART.pause}
          alt="A quiet pause — energy returns on its own"
          size="lg"
        />
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
      {/*
        Only offered when it can actually be taken. A button that explains it is
        unavailable is worse than no button — it turns the pause into a shop
        window. When the gems are short the line below simply states the price,
        which is the one thing worth knowing.
      */}
      {gems !== undefined && (
        canAfford ? (
          <button
            type="button"
            onClick={buyRefill}
            disabled={spending}
            style={{ ...primaryButtonStyle, opacity: spending ? 0.6 : 1 }}
          >
            {spending ? 'SPENDING…' : `REFILL FOR ${GEMS_PER_REFILL} GEMS`}
          </button>
        ) : (
          <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.9rem' }}>
            A full refill costs {GEMS_PER_REFILL} gems — you have {gems}.
          </p>
        )
      )}

      {refillError && (
        <p role="alert" style={{ margin: 0, color: 'var(--danger)', fontSize: '0.9rem' }}>
          {refillError}
        </p>
      )}

      <Link href="/" style={{ ...primaryButtonStyle, display: 'block', textAlign: 'center' }}>
        BACK TO THE PATH
      </Link>
    </section>
  )
}
