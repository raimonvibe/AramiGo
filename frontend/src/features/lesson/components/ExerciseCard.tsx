'use client'

import { useEffect, useState } from 'react'
import {
  ApiError,
  checkAnswer,
  type CheckAnswerResponse,
  type ExerciseView,
  type LearnerStats,
} from '@/shared/lib/api'
import {
  NORMAL_RATE,
  SLOW_RATE,
  onVoicesChanged,
  pronounceable,
  speak,
  speechNotice,
  speechVoiceKind,
  type SpeechAvailability,
} from '@/shared/lib/speech'
import { PlayIcon, SlowPlayIcon } from '@/shared/ui/icons'
import { MatchPairsExercise } from './MatchPairsExercise'
import { primaryButtonStyle } from './lessonStyles'

export { primaryButtonStyle }

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
  }
}

/** Syriac block — chips built from script need the Syriac face and RTL handling. */
function isSyriac(token: string): boolean {
  return /[\u0700-\u074F]/.test(token)
}

/**
 * Show the accepted answer in the same LTR chip order the learner must tap.
 * A single RTL string reverses word order on screen and makes a correct rebuild look wrong.
 */
function AcceptedAnswerHint({ answer }: { answer: string }) {
  const alternatives = answer.split(' or ').map(part => part.replace(/\s*\(one word\)\s*$/, '').trim())
  const hasSyriac = alternatives.some(part => isSyriac(part))

  return (
    <div style={{ marginTop: '0.35rem', color: 'var(--muted)' }}>
      Accepted answer{alternatives.length > 1 ? 's' : ''}:{' '}
      {alternatives.map((phrase, altIndex) => {
        const tokens = phrase.split(/\s+/).filter(Boolean)
        return (
          <span key={`alt-${altIndex}`}>
            {altIndex > 0 && <span style={{ margin: '0 0.35rem' }}>or</span>}
            {hasSyriac && tokens.length > 1 ? (
              <span
                style={{
                  display: 'inline-flex',
                  flexWrap: 'wrap',
                  gap: '0.35rem',
                  direction: 'ltr',
                  verticalAlign: 'middle',
                }}
              >
                {tokens.map((token, tokenIndex) => (
                  <span
                    key={`tok-${altIndex}-${tokenIndex}`}
                    style={{
                      ...chipStyle(true, isSyriac(token)),
                      padding: isSyriac(token) ? '0.15rem 0.55rem' : '0.2rem 0.5rem',
                      fontSize: isSyriac(token) ? '1.15rem' : '0.95rem',
                      unicodeBidi: 'isolate',
                    }}
                  >
                    {token}
                  </span>
                ))}
              </span>
            ) : (
              <strong
                style={{ color: 'var(--text)' }}
                className={isSyriac(phrase) ? 'syriac-inline' : undefined}
              >
                {phrase}
              </strong>
            )}
          </span>
        )
      })}
    </div>
  )
}

export function ExerciseCard({
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
  if (exercise.type === 'MATCH_PAIRS') {
    return (
      <MatchPairsExercise
        exercise={exercise}
        onContinue={onContinue}
        onStats={onStats}
        onOutOfEnergy={onOutOfEnergy}
      />
    )
  }

  return (
    <ChipExerciseCard
      exercise={exercise}
      onContinue={onContinue}
      onStats={onStats}
      onOutOfEnergy={onOutOfEnergy}
    />
  )
}

function ChipExerciseCard({
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
  const [selected, setSelected] = useState<string[]>([])
  const [remaining, setRemaining] = useState(exercise.wordBank)
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState<CheckAnswerResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [voices, setVoices] = useState<SpeechAvailability>('ready')
  const [voiceKind, setVoiceKind] = useState(() =>
    typeof window === 'undefined' ? 'none' : speechVoiceKind(),
  )

  const isListening =
    exercise.type === 'LISTEN_CHOOSE_MEANING' ||
    exercise.type === 'LISTEN_BUILD_ARAMAIC' ||
    exercise.type === 'TAP_WHAT_YOU_HEAR'

  useEffect(() => {
    if (!isListening) return
    return onVoicesChanged(availability => {
      setVoices(availability)
      setVoiceKind(speechVoiceKind())
    })
  }, [isListening])

  const audioText = exercise.audioText ?? exercise.aramaicScript ?? ''
  // Resolved per play, not per render: a Hebrew voice gets Hebrew letters, and
  // the voice list only resolves after mount.
  const spoken = () => pronounceable(exercise.aramaicScript, audioText)
  const notice = voices === 'ready' ? speechNotice() : null

  // Autoplay the prompt on arrival — a listening exercise should start by listening.
  useEffect(() => {
    if (isListening && audioText && voices === 'ready') {
      speak(spoken(), NORMAL_RATE)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [exercise.id, voices])

  async function onCheck() {
    if (selected.length === 0 || busy || feedback?.correct) return
    setBusy(true)
    setError(null)
    try {
      const result = await checkAnswer(exercise.id, selected)
      setFeedback(result)
      onStats(result.stats)
      if (!result.correct) {
        setSelected([])
        setRemaining(exercise.wordBank)
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

  function pick(index: number) {
    if (feedback?.correct) return
    const token = remaining[index]
    setSelected(prev => [...prev, token])
    setRemaining(prev => prev.filter((_, i) => i !== index))
    setFeedback(null)
  }

  function unpick(index: number) {
    if (feedback?.correct) return
    const token = selected[index]
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

      {(exercise.aramaicScript || isListening) && (
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
          {isListening ? (
            <>
              <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                <button
                  type="button"
                  onClick={() => speak(spoken(), NORMAL_RATE)}
                  disabled={voices !== 'ready'}
                  style={{
                    width: 72,
                    height: 72,
                    borderRadius: 18,
                    border: 'none',
                    background: voices === 'ready' ? 'var(--brand)' : '#3a4750',
                    color: voices === 'ready' ? '#1a160c' : '#7d8d97',
                    display: 'grid',
                    placeItems: 'center',
                  }}
                  aria-label="Play audio"
                >
                  <PlayIcon size={30} />
                </button>
                <button
                  type="button"
                  onClick={() => speak(spoken(), SLOW_RATE)}
                  disabled={voices !== 'ready'}
                  style={{
                    width: 52,
                    height: 52,
                    borderRadius: 14,
                    border: 'none',
                    background: voices === 'ready' ? 'var(--accent)' : '#3a4750',
                    color: voices === 'ready' ? '#07130f' : '#7d8d97',
                    display: 'grid',
                    placeItems: 'center',
                  }}
                  aria-label="Play slowly"
                >
                  <SlowPlayIcon size={22} />
                </button>
              </div>

              {notice && (
                <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.85rem', lineHeight: 1.45 }}>
                  {notice}
                  {voiceKind === 'hebrew' ? ' Voice: Hebrew.' : voiceKind === 'other' ? ' Voice: device default.' : ''}
                </p>
              )}

              {voices !== 'ready' && (
                <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.9rem' }}>
                  {voices === 'unsupported'
                    ? 'This browser cannot play audio prompts.'
                    : 'No speech voice is installed on this device, so the audio is silent.'}{' '}
                  The script is shown below instead.
                </p>
              )}

              {/* Fall back to the written form when the audio can't be heard. */}
              {voices !== 'ready' && exercise.aramaicScript && (
                <div className="syriac">{exercise.aramaicScript}</div>
              )}
            </>
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

      {/*
        Keep chip lanes LTR even for Syriac. CSS direction:rtl reverses visual
        order without reversing the token array, so rebuilding the hinted answer
        left-to-right looked right on screen but was submitted backwards.
        Each chip still uses the Syriac face; only the lane order stays logical.
      */}
      <div
        style={{
          minHeight: 72,
          borderBottom: '2px solid var(--line)',
          display: 'flex',
          flexWrap: 'wrap',
          gap: '0.55rem',
          alignItems: 'center',
          paddingBottom: '0.75rem',
          direction: 'ltr',
        }}
      >
        {selected.length === 0 && (
          <span style={{ color: 'var(--muted)' }}>Your answer appears here</span>
        )}
        {selected.map((token, index) => (
          <button
            key={`sel-${token}-${index}`}
            type="button"
            onClick={() => unpick(index)}
            style={{ ...chipStyle(true, isSyriac(token)), unicodeBidi: 'isolate' }}
          >
            {token}
          </button>
        ))}
      </div>

      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: '0.55rem',
          direction: 'ltr',
        }}
      >
        {remaining.map((token, index) => (
          <button
            key={`bank-${token}-${index}`}
            type="button"
            onClick={() => pick(index)}
            disabled={feedback?.correct}
            style={{
              ...chipStyle(false, isSyriac(token)),
              unicodeBidi: 'isolate',
              opacity: feedback?.correct ? 0.45 : 1,
            }}
          >
            {token}
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
          {!feedback.correct && <AcceptedAnswerHint answer={feedback.correctAnswer} />}
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
          {busy ? 'CHECKING…' : 'CHECK'}
        </button>
      )}
    </div>
  )
}
