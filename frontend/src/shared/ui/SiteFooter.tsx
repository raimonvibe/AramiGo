import { SOCIAL_LINKS, SocialIcon } from './SocialIcon'

/** Maker social strip — sits above BottomNav on chrome pages. */
export function SiteFooter() {
  return (
    <footer className="site-footer">
      <p
        style={{
          margin: 0,
          color: 'var(--muted)',
          fontSize: '0.88rem',
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
      <nav aria-label="raimonvibe on the web" className="site-footer-links">
        {SOCIAL_LINKS.map(link => (
          <SocialIcon key={link.id} link={link} size={24} />
        ))}
      </nav>
    </footer>
  )
}
