'use client'

import { useState } from 'react'
import {
  ApiError,
  checkAnswer,
  type CheckAnswerResponse,
  type ExerciseView,
  type LearnerStats,
} from '@/shared/lib/api'
import { primaryButtonStyle } from './lessonStyles'

function isSyriac(token: string): boolean {
  return /[\u0700-\u074F]/.test(token)
}

function chipStyle(selected: boolean, isScript: boolean): Record<string, string | number> {
  return {
    border: '2px solid var(--line)',
    background: selected ? 'rgba(63, 159, 132, 0.2)' : 'var(--bg-chip)',
    color: 'var(--text)',
    borderRadius: 14,
    padding: isScript ? '0.5rem 0.95rem' : '0.65rem 0.9rem',
    fontWeight: 700,
    fontFamily: isScript ? "'Noto Sans Syriac', 'Noto Sans', serif" : 'inherit',
    fontSize: isScript ? '1.35rem' : '1rem',
    lineHeight: isScript ? 1.5 : 1.2,
    unicodeBidi: 'isolate',
  }
}

/**
 * Match Syriac scripts to English meanings. Each completed pair is submitted as
 * {@code script|meaning}; order of pairs does not matter server-side.
 */
export function MatchPairsExercise({
  exercise,
  onContinue,
  onStats,
  onOutOfEnergy,
}: {
  exercise: ExerciseView
  onContinue: () => void
  onStats: (stats: LearnerStats) => void
  onOutOfEnergy: (message: string) => void
}) {
  const scripts = exercise.wordBank.filter(isSyriac)
  const meanings = exercise.wordBank.filter(token => !isSyriac(token))

  const [remainingScripts, setRemainingScripts] = useState(scripts)
  const [remainingMeanings, setRemainingMeanings] = useState(meanings)
  const [selectedScript, setSelectedScript] = useState<string | null>(null)
  const [pairs, setPairs] = useState<{ script: string; meaning: string }[]>([])
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState<CheckAnswerResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  function resetBank() {
    setRemainingScripts(scripts)
    setRemainingMeanings(meanings)
    setSelectedScript(null)
    setPairs([])
  }

  function pickScript(token: string) {
    if (feedback?.correct) return
    setSelectedScript(token)
    setFeedback(null)
  }

  function pickMeaning(token: string) {
    if (feedback?.correct || !selectedScript) return
    const script = selectedScript
    setPairs(prev => [...prev, { script, meaning: token }])
    setRemainingScripts(prev => prev.filter(item => item !== script))
    setRemainingMeanings(prev => prev.filter(item => item !== token))
    setSelectedScript(null)
    setFeedback(null)
  }

  function undoPair(index: number) {
    if (feedback?.correct) return
    const pair = pairs[index]
    setPairs(prev => prev.filter((_, i) => i !== index))
    setRemainingScripts(prev => [...prev, pair.script])
    setRemainingMeanings(prev => [...prev, pair.meaning])
    setFeedback(null)
  }

  async function onCheck() {
    if (pairs.length === 0 || busy || feedback?.correct) return
    setBusy(true)
    setError(null)
    try {
      const tokens = pairs.map(pair => `${pair.script}|${pair.meaning}`)
      const result = await checkAnswer(exercise.id, tokens)
      setFeedback(result)
      onStats(result.stats)
      if (!result.correct) {
        resetBank()
      }
    } catch (err) {
      const apiError = err instanceof ApiError ? err : null
      if (apiError?.code === 'out_of_energy') {
        onOutOfEnergy(apiError.message)
      } else {
        setError(apiError?.message ?? 'Could not check that answer. Please try again.')
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <div style={{ display: 'grid', gap: '1.25rem' }}>
      <div>
        <h2 style={{ margin: 0, fontSize: '1.55rem' }}>{exercise.prompt}</h2>
        {exercise.tip && (
          <p style={{ margin: '0.4rem 0 0', color: 'var(--brand)', fontWeight: 600 }}>
            {exercise.tip}
          </p>
        )}
      </div>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
          gap: '1rem',
        }}
      >
        {/*
          Meaning on the left, Script on the right: the Syriac column belongs on
          the side the script is read from, and its chips flow from that edge.
          The tap order is still script then meaning — which now reads outward
          from the Aramaic rather than across it.
        */}
        <div style={{ display: 'grid', gap: '0.55rem', alignContent: 'start' }}>
          <span style={{ color: 'var(--muted)', fontSize: '0.82rem', fontWeight: 700 }}>
            Meaning
          </span>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.55rem', direction: 'ltr' }}>
            {remainingMeanings.map((token, index) => (
              <button
                key={`meaning-${token}-${index}`}
                type="button"
                onClick={() => pickMeaning(token)}
                disabled={feedback?.correct || !selectedScript}
                style={{
                  ...chipStyle(false, false),
                  opacity: feedback?.correct || !selectedScript ? 0.45 : 1,
                }}
              >
                {token}
              </button>
            ))}
          </div>
        </div>
        <div style={{ display: 'grid', gap: '0.55rem', alignContent: 'start' }}>
          <span
            style={{
              color: 'var(--muted)',
              fontSize: '0.82rem',
              fontWeight: 700,
              textAlign: 'right',
            }}
          >
            Script
          </span>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.55rem', direction: 'rtl' }}>
            {remainingScripts.map((token, index) => (
              <button
                key={`script-${token}-${index}`}
                type="button"
                onClick={() => pickScript(token)}
                disabled={feedback?.correct}
                style={{
                  ...chipStyle(selectedScript === token, true),
                  opacity: feedback?.correct ? 0.45 : 1,
                }}
              >
                {token}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div
        style={{
          minHeight: 72,
          borderBottom: '2px solid var(--line)',
          display: 'flex',
          flexWrap: 'wrap',
          gap: '0.55rem',
          alignItems: 'center',
          paddingBottom: '0.75rem',
        }}
      >
        {pairs.length === 0 && (
          <span style={{ color: 'var(--muted)' }}>
            {selectedScript ? 'Now tap a meaning' : 'Tap a script, then its meaning'}
          </span>
        )}
        {pairs.map((pair, index) => (
          <button
            key={`${pair.script}-${pair.meaning}-${index}`}
            type="button"
            onClick={() => undoPair(index)}
            style={{
              ...chipStyle(true, false),
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.45rem',
            }}
          >
            <span>{pair.meaning}</span>
            <span style={{ color: 'var(--muted)' }}>=</span>
            <span className="syriac-inline" style={{ fontSize: '1.2rem' }}>
              {pair.script}
            </span>
          </button>
        ))}
      </div>

      {error && (
        <div
          role="alert"
          style={{
            borderRadius: 'var(--radius)',
            padding: '0.85rem 1rem',
            background: 'rgba(224, 122, 95, 0.15)',
            border: '1px solid var(--danger)',
          }}
        >
          {error}
        </div>
      )}

      {feedback && (
        <div
          role="status"
          style={{
            borderRadius: 'var(--radius)',
            padding: '1rem',
            background: feedback.correct ? 'rgba(63, 159, 132, 0.18)' : 'rgba(224, 122, 95, 0.15)',
            border: `1px solid ${feedback.correct ? 'var(--accent)' : 'var(--danger)'}`,
          }}
        >
          <strong>{feedback.message}</strong>
          {!feedback.correct && (
            <div style={{ marginTop: '0.35rem', color: 'var(--muted)' }}>
              Accepted: <strong style={{ color: 'var(--text)' }}>{feedback.correctAnswer}</strong>
            </div>
          )}
        </div>
      )}

      {feedback?.correct ? (
        <button type="button" onClick={onContinue} style={primaryButtonStyle}>
          CONTINUE
        </button>
      ) : (
        <button
          type="button"
          onClick={onCheck}
          disabled={pairs.length === 0 || remainingScripts.length > 0 || busy}
          style={{
            ...primaryButtonStyle,
            background:
              pairs.length === 0 || remainingScripts.length > 0 ? '#314049' : 'var(--accent)',
            color: pairs.length === 0 || remainingScripts.length > 0 ? '#7d8d97' : '#07130f',
          }}
        >
          {busy ? 'CHECKING…' : 'CHECK'}
        </button>
      )}
    </div>
  )
}
