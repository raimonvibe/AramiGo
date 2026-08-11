import type { LearnerStats } from '@/shared/lib/api'

function energyHint(stats: LearnerStats): string {
  if (stats.energy >= stats.maxEnergy) return 'Energy: full'
  const minutes = Math.ceil(stats.secondsUntilNextEnergy / 60)
  return `Energy: ${stats.energy} of ${stats.maxEnergy} — next point in about ${minutes} min`
}

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
      <span title={`Streak: ${stats.streak} day${stats.streak === 1 ? '' : 's'}`} style={{ color: 'var(--streak)' }}>
        ✦ {stats.streak}
      </span>
      <span title={`Gems: ${stats.gems}`} style={{ color: 'var(--gem)' }}>
        ◆ {stats.gems}
      </span>
      <span title={energyHint(stats)} style={{ color: 'var(--energy)' }}>
        ⚡ {stats.energy}
      </span>
    </div>
  )
}
