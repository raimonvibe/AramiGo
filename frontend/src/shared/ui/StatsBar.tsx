import type { LearnerStats } from '@/shared/lib/api'

export function StatsBar({ stats }: { stats: LearnerStats }) {
  return (
    <div
      style={{
        display: 'flex',
        gap: '1rem',
        alignItems: 'center',
        justifyContent: 'flex-end',
        fontWeight: 700,
      }}
    >
      <span title="Streak" style={{ color: 'var(--streak)' }}>
        ✦ {stats.streak}
      </span>
      <span title="Gems" style={{ color: 'var(--gem)' }}>
        ◆ {stats.gems}
      </span>
      <span title="Energy" style={{ color: 'var(--energy)' }}>
        ⚡ {stats.energy}
      </span>
    </div>
  )
}
