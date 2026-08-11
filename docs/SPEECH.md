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

## Long-term

Ship **human recordings** (Western Classical to match `shlomo`-style romanization) as MP3/OGG per prompt, and keep Web Speech only as fallback.
