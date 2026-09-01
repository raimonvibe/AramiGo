'use client'

import Script from 'next/script'
import { useCallback, useEffect, useRef, useState } from 'react'
import {
  ApiError,
  clearToken,
  getProfile,
  linkGuestProgress,
  storeToken,
  storedToken,
} from '@/shared/lib/api'
import {
  GOOGLE_CLIENT_ID,
  GOOGLE_SIGN_IN_ENABLED,
  GSI_SCRIPT_SRC,
  googleIdentity,
  nameFromIdToken,
  type GoogleCredentialResponse,
} from './googleIdentity'
import { notifyAccountChanged } from '@/shared/lib/accountEvents'

/**
 * Sign-in block, shown inside the app menu so it is reachable from every page.
 *
 * Signing in is optional and additive: guests keep full access, and the progress
 * they already made is merged into the account the first time they sign in.
 */
export function AccountBar({ onAccountChanged }: { onAccountChanged?: () => void } = {}) {
  const [displayName, setDisplayName] = useState<string | null>(null)
  const [signedIn, setSignedIn] = useState(false)
  const [status, setStatus] = useState<string | null>(null)
  const buttonHost = useRef<HTMLDivElement | null>(null)
  const [scriptReady, setScriptReady] = useState(false)

  useEffect(() => {
    if (!storedToken()) return
    getProfile()
      .then(profile => {
        setSignedIn(profile.signedIn)
        setDisplayName(profile.displayName)
      })
      .catch(() => {
        // An expired token is already cleared by the API client; fall back to guest.
        setSignedIn(false)
        setDisplayName(null)
      })
  }, [])

  const onCredential = useCallback(
    async (response: GoogleCredentialResponse) => {
      storeToken(response.credential)
      setDisplayName(nameFromIdToken(response.credential))
      setStatus('Bringing your progress across…')
      try {
        const profile = await linkGuestProgress()
        setSignedIn(profile.signedIn)
        setDisplayName(profile.displayName ?? nameFromIdToken(response.credential))
        setStatus(null)
        onAccountChanged?.()
        notifyAccountChanged()
      } catch (err) {
        clearToken()
        setSignedIn(false)
        setStatus(
          err instanceof ApiError ? err.message : 'Could not finish signing in. Please try again.',
        )
      }
    },
    [onAccountChanged],
  )

  // Render Google's own button — its look and consent flow are prescribed by Google.
  useEffect(() => {
    if (!scriptReady || signedIn || !buttonHost.current) return
    const identity = googleIdentity()
    if (!identity) return

    identity.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: response => void onCredential(response),
      cancel_on_tap_outside: true,
    })
    identity.renderButton(buttonHost.current, {
      type: 'standard',
      theme: 'filled_black',
      size: 'medium',
      shape: 'pill',
      text: 'continue_with',
    })
  }, [scriptReady, signedIn, onCredential])

  function signOut() {
    googleIdentity()?.disableAutoSelect()
    clearToken()
    setSignedIn(false)
    setDisplayName(null)
    setStatus(null)
    onAccountChanged?.()
    notifyAccountChanged()
  }

  if (!GOOGLE_SIGN_IN_ENABLED) {
    return null
  }

  return (
    <>
      <Script src={GSI_SCRIPT_SRC} onReady={() => setScriptReady(true)} />
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '0.75rem',
          flexWrap: 'wrap',
          padding: '0.7rem 0.9rem',
          marginBottom: '1.25rem',
          borderRadius: 14,
          border: '1px solid var(--line)',
          background: 'var(--bg-elevated)',
        }}
      >
        {signedIn ? (
          <>
            <span style={{ fontSize: '0.92rem' }}>
              Signed in{displayName ? ' as ' : ''}
              {displayName && <strong style={{ color: 'var(--brand)' }}>{displayName}</strong>}
              <span style={{ display: 'block', color: 'var(--muted)', fontSize: '0.8rem' }}>
                Your progress is saved to your account.
              </span>
            </span>
            <button
              type="button"
              onClick={signOut}
              style={{
                border: '1px solid var(--line)',
                background: 'transparent',
                color: 'var(--muted)',
                borderRadius: 999,
                padding: '0.4rem 0.9rem',
                fontWeight: 700,
                fontSize: '0.85rem',
              }}
            >
              Sign out
            </button>
          </>
        ) : (
          <>
            <span style={{ fontSize: '0.85rem', color: 'var(--muted)', maxWidth: '22ch' }}>
              Sign in to keep your progress on any device.
            </span>
            <div ref={buttonHost} />
          </>
        )}
      </div>
      {status && (
        <p role="status" style={{ margin: '-0.75rem 0 1rem', color: 'var(--muted)' }}>
          {status}
        </p>
      )}
    </>
  )
}
