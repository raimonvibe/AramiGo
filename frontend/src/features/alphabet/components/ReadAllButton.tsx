'use client'

/** Starts a whole-list run, and turns into the stop control while one is going. */
export function ReadAllButton({
  ready,
  running,
  label,
  onPlayAll,
  onStop,
}: {
  ready: boolean
  running: boolean
  label: string
  onPlayAll: () => void
  onStop: () => void
}) {
  return (
    <button
      type="button"
      onClick={running ? onStop : onPlayAll}
      disabled={!ready}
      style={{
        border: 'none',
        borderRadius: 14,
        padding: '0.6rem 1.1rem',
        fontWeight: 800,
        background: ready ? 'var(--brand)' : '#3a4750',
        color: ready ? '#1a160c' : '#7d8d97',
      }}
    >
      {running ? 'Stop' : label}
    </button>
  )
}
