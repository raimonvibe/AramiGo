/**
 * Sign-in and sign-out now happen in the app menu, which is rendered beside the
 * page rather than inside it. This lets whatever is showing progress hear about
 * it without the shell having to know which page is mounted.
 */

const ACCOUNT_CHANGED = 'aramigo:account-changed'

export function notifyAccountChanged() {
  if (typeof window === 'undefined') return
  window.dispatchEvent(new Event(ACCOUNT_CHANGED))
}

/** Subscribe to sign-in / sign-out. Returns an unsubscribe function. */
export function onAccountChanged(listener: () => void): () => void {
  if (typeof window === 'undefined') return () => {}
  window.addEventListener(ACCOUNT_CHANGED, listener)
  return () => window.removeEventListener(ACCOUNT_CHANGED, listener)
}
