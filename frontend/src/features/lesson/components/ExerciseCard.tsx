'use client'

import { useMemo, useState } from 'react'
import {
  checkAnswer,
  type CheckAnswerResponse,
  type ExerciseView,
  type LearnerStats,
} from '@/shared/lib/api'

function speak(text: string) {
  if (typeof window === 'undefined' || !window.speechSynthesis) return
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.rate = 0.85
  window.speechSynthesis.speak(utterance)
}

const primaryButtonStyle: Record<string, string | number> = {
  width: '100%',
  border: 'none',
  borderRadius: 16,
  padding: '1rem',
  fontWeight: 800,
  letterSpacing: '0.06em',
  background: 'var(--accent)',
  color: '#07130f',
}

function chipStyle(selected: boolean): Record<string, string | number> {
  return {
    border: '2px solid var(--line)',
    background: selected ? 'rgba(63, 159, 132, 0.2)' : 'var(--bg-chip)',
    color: 'var(--text)',
    borderRadius: 14,
    padding: '0.65rem 0.9rem',
    fontWeight: 700,
  }
}

export function ExerciseCard({
  exercise,
  onContinue,
  onStats,
}: {
  exercise: ExerciseView
  onContinue: () => void
  onStats: (stats: LearnerStats) => void
}) {
  const [selected, setSelected] = useState<string[]>([])
  const [remaining, setRemaining] = useState(exercise.wordBank)
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState<CheckAnswerResponse | null>(null)

  const promptText = useMemo(() => {
    if (exercise.type === 'LISTEN') {
      return exercise.transliteration ?? exercise.aramaicScript ?? ''
    }
    return exercise.transliteration ?? ''
  }, [exercise])

  async function onCheck() {
    if (selected.length === 0 || busy || feedback?.correct) return
    setBusy(true)
    try {
      const result = await checkAnswer(exercise.id, selected)
      setFeedback(result)
      onStats(result.stats)
      if (!result.correct) {
        setSelected([])
        setRemaining(exercise.wordBank)
      }
    } finally {
      setBusy(false)
    }
  }

  function pick(token: string, index: number) {
    if (feedback?.correct) return
    setSelected(prev => [...prev, token])
    setRemaining(prev => prev.filter((_, i) => i !== index))
    setFeedback(null)
  }

  function unpick(token: string, index: number) {
    if (feedback?.correct) return
    setSelected(prev => prev.filter((_, i) => i !== index))
    setRemaining(prev => [...prev, token])
    setFeedback(null)
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

      {(exercise.aramaicScript || exercise.type === 'LISTEN') && (
        <div
          style={{
            background: 'var(--bg-elevated)',
            border: '1px solid var(--line)',
            borderRadius: 'var(--radius)',
            padding: '1.25rem',
            display: 'grid',
            gap: '0.75rem',
            justifyItems: 'start',
          }}
        >
          {exercise.type === 'LISTEN' ? (
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
              <button
                type="button"
                onClick={() => speak(promptText)}
                style={{
                  width: 72,
                  height: 72,
                  borderRadius: 18,
                  border: 'none',
                  background: '#7eb6ff',
                  color: '#102033',
                  fontSize: '1.6rem',
                  fontWeight: 800,
                }}
                aria-label="Play audio"
              >
                ♪
              </button>
              <button
                type="button"
                onClick={() => speak(promptText)}
                style={{
                  width: 52,
                  height: 52,
                  borderRadius: 14,
                  border: 'none',
                  background: '#5f91d3',
                  color: '#102033',
                  fontWeight: 800,
                }}
                aria-label="Play slowly"
              >
                🐢
              </button>
              {exercise.transliteration && (
                <span style={{ color: 'var(--muted)', fontSize: '0.95rem' }}>
                  (sounds like “{exercise.transliteration}”)
                </span>
              )}
            </div>
          ) : (
            <>
              <div className="syriac">{exercise.aramaicScript}</div>
              {exercise.transliteration && (
                <div style={{ color: 'var(--muted)' }}>
                  Sounds like:{' '}
                  <strong style={{ color: 'var(--text)' }}>{exercise.transliteration}</strong>
                </div>
              )}
            </>
          )}
        </div>
      )}

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
        {selected.length === 0 && (
          <span style={{ color: 'var(--muted)' }}>Your answer appears here</span>
        )}
        {selected.map((token, index) => (
          <button
            key={`sel-${token}-${index}`}
            type="button"
            onClick={() => unpick(token, index)}
            style={chipStyle(true)}
          >
            {token}
          </button>
        ))}
      </div>

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.55rem' }}>
        {remaining.map((token, index) => (
          <button
            key={`bank-${token}-${index}`}
            type="button"
            onClick={() => pick(token, index)}
            style={chipStyle(false)}
          >
            {token}
          </button>
        ))}
      </div>

      {feedback && (
        <div
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
              Accepted answer{feedback.correctAnswer.includes(' or ') ? 's' : ''}:{' '}
              <strong style={{ color: 'var(--text)' }}>{feedback.correctAnswer}</strong>
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
          disabled={selected.length === 0 || busy}
          style={{
            ...primaryButtonStyle,
            background: selected.length === 0 ? '#314049' : 'var(--accent)',
            color: selected.length === 0 ? '#7d8d97' : '#07130f',
          }}
        >
          CHECK
        </button>
      )}
    </div>
  )
}

export { primaryButtonStyle }
