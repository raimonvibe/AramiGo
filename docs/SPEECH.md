# AramiGo — speech / listen mode

Classical Syriac has **no** built-in TTS voice in browsers or major cloud APIs.

## Current approach

Listen prompts play the exercise **romanization** (e.g. `shlomo`) through the Web Speech API with:

1. Preferred voice: **Hebrew** (`he` / `iw` / `he-IL`)
2. Utterance language: `he-IL`
3. Fallback: any other installed system voice, with an on-screen warning

Visitors are told this is a **Hebrew stand-in**, not authentic Syriac or Galilean Aramaic (language of Jesus).

Code: `frontend/src/shared/lib/speech.ts`  
UI notices: learning path header, lesson player banner, listen exercise card.

## Why Hebrew

Same Semitic family as Aramaic; consonants are closer than an English default. It is still a rough approximation.

## Linux users — how to get speech at all

On many Linux desktops the Web Speech API finds **no voices**, so listen mode stays silent. Hebrew is especially poorly wired through the usual Linux stack (`speech-dispatcher` + `espeak-ng`); MBROLA Hebrew voices exist but often do **not** show up cleanly in the browser.

### Practical options (easiest first)

**1. Use Chrome or Edge while online (best shot at Hebrew)**  
Chromium-based browsers can expose **Google cloud voices** (including `he-IL`) when online. Firefox on Linux is usually weaker for this.

1. Open AramiGo in Chrome/Edge  
2. Allow the site to play sound  
3. In DevTools console, run:

```js
speechSynthesis.getVoices().map(v => [v.name, v.lang])
```

If you see a `he` / `he-IL` / `iw` entry, AramiGo will prefer it.

**2. Install any local TTS so the app is not silent (fallback voice)**  
Debian/Ubuntu:

```bash
sudo apt update
sudo apt install speech-dispatcher speech-dispatcher-espeak-ng espeak-ng
# optional check:
spd-say "hello"
espeak-ng "shlomo"
```

Then restart the browser. You may only get an **English/other** voice; the UI will say it fell back. That is still better than silence.

**3. Do not expect easy native Hebrew on Linux**  
`espeak-ng` Hebrew / MBROLA `hb1`/`hb2` support is incomplete in many distro packages. Building from source or custom speech-dispatcher modules is possible but not worth it for most learners.

**4. Best for Linux (and everyone) long-term**  
Ship **recorded MP3/OGG** clips with the curriculum so listen mode does not depend on the OS voice pack.

### Quick self-check

| Result in `getVoices()` | What AramiGo does |
|-------------------------|-------------------|
| Hebrew (`he` / `iw`) present | Uses Hebrew stand-in |
| Only other languages | Uses device default + fallback notice |
| Empty list | Silent; shows script fallback |

## Long-term

Ship **human recordings** (Western Classical to match `shlomo`-style romanization) as MP3/OGG per prompt, and keep Web Speech only as fallback.
