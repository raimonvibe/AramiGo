import { SOCIAL_LINKS, SocialIcon } from './SocialIcon'

/** Maker social strip — sits above BottomNav on chrome pages. */
export function SiteFooter() {
  return (
    <footer
      style={{
        width: 'min(560px, 100%)',
        margin: '0 auto',
        padding: '1.25rem 1rem 0.85rem',
        borderTop: '1px solid var(--line)',
        display: 'grid',
        gap: '0.75rem',
        justifyItems: 'center',
      }}
    >
      <p
        style={{
          margin: 0,
          color: 'var(--muted)',
          fontSize: '0.82rem',
          textAlign: 'center',
        }}
      >
        Made by{' '}
        <a
          href="https://www.raimonvibe.eu/"
          target="_blank"
          rel="noopener noreferrer me"
          style={{ color: 'var(--brand)', fontWeight: 700 }}
        >
          raimonvibe
        </a>
      </p>
      <nav
        aria-label="raimonvibe on the web"
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          justifyContent: 'center',
          gap: '0.15rem',
          maxWidth: '100%',
        }}
      >
        {SOCIAL_LINKS.map(link => (
          <SocialIcon key={link.id} link={link} />
        ))}
      </nav>
    </footer>
  )
}
