const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

export type NodeStatus = 'LOCKED' | 'CURRENT' | 'COMPLETED'
export type ExerciseType = 'TRANSLATE_TO_ENGLISH' | 'TRANSLATE_TO_ARAMAIC' | 'LISTEN'

export interface LearnerStats {
  energy: number
  gems: number
  streak: number
}

export interface PathNode {
  lessonId: number
  position: number
  title: string
  nodeKind: string
  status: NodeStatus
}

export interface LearningPath {
  sectionNumber: number
  unitNumber: number
  title: string
  description: string
  stats: LearnerStats
  nodes: PathNode[]
}

export interface ExerciseView {
  id: number
  type: ExerciseType
  prompt: string
  tip: string | null
  aramaicScript: string | null
  transliteration: string | null
  wordBank: string[]
}

export interface LessonSession {
  lessonId: number
  title: string
  stats: LearnerStats
  exercises: ExerciseView[]
}

export interface CheckAnswerResponse {
  correct: boolean
  message: string
  correctAnswer: string
  energyDelta: number
  stats: LearnerStats
}

export interface CompleteLessonResponse {
  energyReward: number
  gemsReward: number
  stats: LearnerStats
}

function guestKey(): string {
  if (typeof window === 'undefined') return 'guest'
  const existing = localStorage.getItem('aramigo-guest-key')
  if (existing) return existing
  const created = `guest-${crypto.randomUUID()}`
  localStorage.setItem('aramigo-guest-key', created)
  return created
}

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      'X-Guest-Key': guestKey(),
      ...(init?.headers ?? {}),
    },
    cache: 'no-store',
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `Request failed (${response.status})`)
  }

  return response.json() as Promise<T>
}

export const getPath = () => api<LearningPath>('/api/path')

export const getLesson = (lessonId: number) =>
  api<LessonSession>(`/api/lessons/${lessonId}`)

export const checkAnswer = (exerciseId: number, tokens: string[]) =>
  api<CheckAnswerResponse>('/api/exercises/check', {
    method: 'POST',
    body: JSON.stringify({ exerciseId, tokens }),
  })

export const completeLesson = (lessonId: number) =>
  api<CompleteLessonResponse>('/api/lessons/complete', {
    method: 'POST',
    body: JSON.stringify({ lessonId }),
  })
