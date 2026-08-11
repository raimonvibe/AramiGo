/**
 * Minimal typing for the slice of Google Identity Services we use.
 *
 * The browser never sees a client secret: GIS returns a signed ID token that the
 * API verifies against Google's public keys. Nothing here is trusted client-side.
 */
export interface GoogleCredentialResponse {
  credential: string
}

interface GoogleIdApi {
  initialize(config: {
    client_id: string
    callback: (response: GoogleCredentialResponse) => void
    auto_select?: boolean
    cancel_on_tap_outside?: boolean
    use_fedcm_for_prompt?: boolean
  }): void
  renderButton(
    parent: HTMLElement,
    options: {
      type?: 'standard' | 'icon'
      theme?: 'outline' | 'filled_blue' | 'filled_black'
      size?: 'small' | 'medium' | 'large'
      shape?: 'rectangular' | 'pill'
      text?: 'signin_with' | 'signup_with' | 'continue_with'
      width?: number
    },
  ): void
  disableAutoSelect(): void
}

declare global {
  interface Window {
    google?: { accounts: { id: GoogleIdApi } }
  }
}

export const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ?? ''

export const GOOGLE_SIGN_IN_ENABLED = GOOGLE_CLIENT_ID.length > 0

export const GSI_SCRIPT_SRC = 'https://accounts.google.com/gsi/client'

export function googleIdentity(): GoogleIdApi | null {
  if (typeof window === 'undefined') return null
  return window.google?.accounts.id ?? null
}

/** Reads the display name out of an ID token for an instant greeting. */
export function nameFromIdToken(idToken: string): string | null {
  try {
    const payload = idToken.split('.')[1]
    if (!payload) return null
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    const claims = JSON.parse(json) as { name?: string; given_name?: string }
    return claims.given_name ?? claims.name ?? null
  } catch {
    return null
  }
}
