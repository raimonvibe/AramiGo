import type { ReactNode } from 'react'
import type { LearnerStats } from '@/shared/lib/api'
import { EnergyIcon, GemIcon, StreakIcon } from './icons'

function energyHint(stats: LearnerStats): string {
  if (stats.energy >= stats.maxEnergy) return 'Energy: full'
  const minutes = Math.ceil(stats.secondsUntilNextEnergy / 60)
  return `Energy: ${stats.energy} of ${stats.maxEnergy} — next point in about ${minutes} min`
}

function StatChip({
  title,
  color,
  icon,
  value,
}: {
  title: string
  color: string
  icon: ReactNode
  value: number
}) {
  return (
    <span
      title={title}
      style={{
        color,
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.3rem',
      }}
    >
      {icon}
      {value}
    </span>
  )
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
      <StatChip
        title={`Streak: ${stats.streak} day${stats.streak === 1 ? '' : 's'}`}
        color="var(--streak)"
        icon={<StreakIcon size={16} />}
        value={stats.streak}
      />
      <StatChip
        title={`Gems: ${stats.gems}`}
        color="var(--gem)"
        icon={<GemIcon size={16} />}
        value={stats.gems}
      />
      <StatChip
        title={energyHint(stats)}
        color="var(--energy)"
        icon={<EnergyIcon size={16} />}
        value={stats.energy}
      />
    </div>
  )
}
