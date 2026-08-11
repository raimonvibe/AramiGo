const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

export type NodeStatus = 'LOCKED' | 'CURRENT' | 'COMPLETED'
export type NodeKind = 'STAR' | 'CHEST' | 'CHARACTER'
export type ExerciseType =
  | 'TRANSLATE_TO_ENGLISH'
  | 'TRANSLATE_TO_ARAMAIC'
  | 'LISTEN_CHOOSE_MEANING'
  | 'LISTEN_BUILD_ARAMAIC'
  | 'MATCH_PAIRS'
  | 'TAP_WHAT_YOU_HEAR'

export interface LearnerStats {
  energy: number
  maxEnergy: number
  gems: number
  streak: number
  secondsUntilNextEnergy: number
}

export interface PathNode {
  lessonId: number
  position: number
  title: string
  nodeKind: NodeKind
  status: NodeStatus
  exerciseCount: number
  solvedCount: number
}

export interface PathUnit {
  sectionNumber: number
  unitNumber: number
  title: string
  description: string
  nodes: PathNode[]
}

export interface LearningPath {
  stats: LearnerStats
  units: PathUnit[]
}

export interface ExerciseView {
  id: number
  type: ExerciseType
  prompt: string
  tip: string | null
  aramaicScript: string | null
  /** Withheld by the API when showing it would give the answer away. */
  transliteration: string | null
  /** Pronunciation text for playback; only present on listening exercises. */
  audioText: string | null
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

export interface Profile {
  signedIn: boolean
  displayName: string | null
  email: string | null
  pictureUrl: string | null
  stats: LearnerStats
  completedLessons: number
  totalLessons: number
}

/** Machine-readable reasons the UI reacts to; anything else is just a message. */
export type ApiErrorCode =
  | 'not_found'
  | 'unauthorized'
  | 'lesson_locked'
  | 'lesson_incomplete'
  | 'out_of_energy'
  | 'invalid_request'
  | 'server_error'
  | 'network'

export class ApiError extends Error {
  readonly code: ApiErrorCode
  readonly status: number
  readonly retryAfterSeconds: number | null

  constructor(
    code: ApiErrorCode,
    message: string,
    status: number,
    retryAfterSeconds: number | null = null,
  ) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
    this.retryAfterSeconds = retryAfterSeconds
  }
}

const GUEST_KEY_STORAGE = 'aramigo-guest-key'
const TOKEN_STORAGE = 'aramigo-google-token'

export function guestKey(): string {
  if (typeof window === 'undefined') return 'guest'
  const existing = localStorage.getItem(GUEST_KEY_STORAGE)
  if (existing) return existing
  const created = `guest-${crypto.randomUUID()}`
  localStorage.setItem(GUEST_KEY_STORAGE, created)
  return created
}

export function storedToken(): string | null {
  if (typeof window === 'undefined') return null
  return localStorage.getItem(TOKEN_STORAGE)
}

export function storeToken(token: string) {
  localStorage.setItem(TOKEN_STORAGE, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_STORAGE)
}

/**
 * Turns any failure into an ApiError carrying a message a learner can read.
 * The raw response body never reaches the screen.
 */
async function toApiError(response: Response): Promise<ApiError> {
  const retryAfterHeader = response.headers.get('Retry-After')
  const retryAfter = retryAfterHeader ? Number(retryAfterHeader) : null

  let code: ApiErrorCode = 'server_error'
  let message = 'Something went wrong. Please try again.'

  try {
    const body = await response.json()
    if (typeof body?.code === 'string') code = body.code as ApiErrorCode
    if (typeof body?.message === 'string' && body.message.length > 0) message = body.message
  } catch {
    // A non-JSON body (proxy error page, empty 502) — keep the generic message.
  }

  return new ApiError(code, message, response.status, Number.isFinite(retryAfter) ? retryAfter : null)
}

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const token = storedToken()

  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        'X-Guest-Key': guestKey(),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(init?.headers ?? {}),
      },
      cache: 'no-store',
    })
  } catch {
    throw new ApiError('network', "Can't reach AramiGo right now. Check your connection.", 0)
  }

  if (!response.ok) {
    const error = await toApiError(response)
    // An expired sign-in shouldn't strand anyone — drop it and continue as a guest.
    if (error.code === 'unauthorized') clearToken()
    throw error
  }

  return response.json() as Promise<T>
}

export const getPath = () => api<LearningPath>('/api/path')

export const getLesson = (lessonId: number) => api<LessonSession>(`/api/lessons/${lessonId}`)

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

export const getProfile = () => api<Profile>('/api/me')

export const linkGuestProgress = () => api<Profile>('/api/auth/link', { method: 'POST' })
