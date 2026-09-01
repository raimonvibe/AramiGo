# AramiGo — speech / listen mode

Classical Syriac has **no** text-to-speech voice. Not in browsers, not in the
major cloud APIs, not as an open model. That is not a gap waiting to be filled
here: it is the permanent condition this feature is designed around.

## The decision

**AramiGo will not ship recorded audio.** Listen mode is a Hebrew system voice
reading Syriac letters, and that is the approach — not a stopgap, not a
placeholder for a recording project that is coming later.

This matters mostly because the app must not imply otherwise. Every listening
surface says plainly that the voice is a stand-in and not authentic Syriac, and
those notices are permanent copy, not temporary apologies.

If that changes one day, the shape of the work is: an `audioClip` field per
exercise in `curriculum/*.json`, clips served from `public/`, and Web Speech kept
as the fallback where a clip is missing. Nothing in the current design blocks it.
Nothing in the current plan pursues it either.

## How it actually works

Listen prompts hand the engine **Syriac rewritten in Hebrew letters**, not the
Latin romanization — see `toHebrewScript` and `pronounceable` in
`frontend/src/shared/lib/speech.ts`.

Both scripts are the same 22-letter Aramaic abjad in the same order, so the
mapping is one-to-one rather than a transliteration scheme. Handed ܚܕ as חד a
Hebrew voice pronounces a Semitic word; handed the Latin `had` it reads English.
Syriac vowel points are dropped rather than converted, because the content here
is mostly unpointed and a half-pointed word reads worse than a bare consonantal
one.

The romanization is still the fallback, and it is what a device without a Hebrew
voice receives — an English voice can at least make something of `shlomo`, and
can make nothing of חד.

1. Preferred voice: **Hebrew** (`he` / `iw` / `he-IL`)
2. Utterance language: `he-IL`
3. Fallback: any other installed voice, reading the romanization, with an
   on-screen warning

Single glyphs are special-cased to speak the Hebrew letter *name* (אלף, בית, …),
because engines choke on a bare letter — alef and ayin go silent on Windows'
Asaf, and others come out as a clipped phoneme.

UI notices live on the learning path header, the lesson player banner, and the
listen exercise card.

## Why Hebrew

Same Semitic family, same abjad, and consonants far closer than an English
default. It remains a rough approximation of Classical Syriac, and no closer to
Galilean Aramaic — the dialect of Jesus — which the app also says out loud,
because people arrive looking for exactly that.

## Linux users — how to get any speech at all

On many Linux desktops the Web Speech API finds **no voices**, so listen mode is
silent. Hebrew is especially poorly wired through the usual stack
(`speech-dispatcher` + `espeak-ng`); MBROLA Hebrew voices exist but often do not
surface cleanly in the browser.

**1. Use Chrome or Edge while online — the best shot at Hebrew.**
Chromium-based browsers can expose Google cloud voices including `he-IL`. Firefox
on Linux is usually weaker. Check with:

```js
speechSynthesis.getVoices().map(v => [v.name, v.lang])
```

If a `he` / `he-IL` / `iw` entry appears, AramiGo will prefer it.

**2. Install any local TTS so the app is not silent.**
Debian/Ubuntu:

```bash
sudo apt update
sudo apt install speech-dispatcher speech-dispatcher-espeak-ng espeak-ng
```

Then restart the browser. You may only get an English voice, which reads the
romanization rather than the script — the UI will say it fell back. Still better
than silence.

**3. Do not expect native Hebrew on Linux.** `espeak-ng` Hebrew and MBROLA
`hb1`/`hb2` support is incomplete in most distro packages. Building from source
is possible and is not worth it for a stand-in voice.

**4. If none of that works, the script is still shown.** A learner with no speech
engine sees the Syriac written out in place of the audio and can complete every
listening exercise by reading it. That is the honest floor here, and with no
recordings planned it is also the ceiling on those machines.

### Quick self-check

| Result in `getVoices()` | What AramiGo does |
|-------------------------|-------------------|
| Hebrew (`he` / `iw`) present | Hebrew voice, reading Syriac mapped to Hebrew letters |
| Only other languages | Device default, reading the romanization, plus a notice |
| Empty list | Silent; the script is shown instead |
