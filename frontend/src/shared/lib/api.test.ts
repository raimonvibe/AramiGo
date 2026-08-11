import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, getPath, storedToken } from './api'

/**
 * These cover the rule that a raw response body must never reach the screen:
 * every failure becomes an ApiError with a message written for a learner.
 */

const storage = new Map<string, string>()

function installBrowserGlobals() {
  vi.stubGlobal('localStorage', {
    getItem: (key: string) => storage.get(key) ?? null,
    setItem: (key: string, value: string) => void storage.set(key, value),
    removeItem: (key: string) => void storage.delete(key),
  })
  vi.stubGlobal('window', { localStorage: true })
  vi.stubGlobal('crypto', { randomUUID: () => 'test-uuid' })
}

function jsonResponse(status: number, body: unknown, headers: Record<string, string> = {}) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json', ...headers },
  })
}

describe('api client', () => {
  beforeEach(() => {
    storage.clear()
    installBrowserGlobals()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('turns a structured API error into a readable message', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(403, { status: 'error', code: 'lesson_locked', message: 'Lesson is still locked' }),
      ),
    )

    await expect(getPath()).rejects.toMatchObject({
      code: 'lesson_locked',
      message: 'Lesson is still locked',
      status: 403,
    })
  })

  it('never leaks a non-JSON body to the learner', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValue(new Response('<html>502 Bad Gateway</html>', { status: 502 })),
    )

    const error = await getPath().catch((err: unknown) => err)

    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).message).toBe('Something went wrong. Please try again.')
    expect((error as ApiError).message).not.toContain('html')
  })

  it('reports an unreachable API as a network problem, not a crash', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    const error = await getPath().catch((err: unknown) => err)

    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).code).toBe('network')
  })

  it('surfaces how long until energy returns', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(
          429,
          { status: 'error', code: 'out_of_energy', message: 'Out of energy' },
          { 'Retry-After': '420' },
        ),
      ),
    )

    const error = (await getPath().catch((err: unknown) => err)) as ApiError

    expect(error.code).toBe('out_of_energy')
    expect(error.retryAfterSeconds).toBe(420)
  })

  it('drops an expired sign-in so the learner falls back to guest access', async () => {
    storage.set('aramigo-google-token', 'expired-token')
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(401, { status: 'error', code: 'unauthorized', message: 'Sign-in expired' }),
      ),
    )

    await expect(getPath()).rejects.toMatchObject({ code: 'unauthorized' })
    expect(storedToken()).toBeNull()
  })

  it('sends the bearer token when signed in', async () => {
    storage.set('aramigo-google-token', 'valid-token')
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, { stats: {}, units: [] }))
    vi.stubGlobal('fetch', fetchMock)

    await getPath()

    const headers = fetchMock.mock.calls[0][1].headers as Record<string, string>
    expect(headers.Authorization).toBe('Bearer valid-token')
    expect(headers['X-Guest-Key']).toBeTruthy()
  })
})
