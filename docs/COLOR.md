# AramiGo — color & gradients (anti-Duolingo)

Reference for brand color choices as the curriculum grows.  
Sources: [Duolingo brand colors](https://design.duolingo.com/identity/color#core-brand-colors) and common product UI notes (flat solids, no soft atmospheric gradients).

## Duolingo — do not copy

| Role | Hex |
|------|-----|
| Feather Green (core) | `#58CC02` |
| Mask Green | `#89E219` |
| Macaw blue | `#1CB0F6` |
| Cardinal red | `#FF4B4B` |
| Bee yellow | `#FFC800` |
| Fox orange | `#FF9600` |
| Beetle purple | `#CE82FF` |
| Humpback blue | `#2B70C9` |
| Canvas | white `#FFFFFF` + gray “Eel” text |

Their product UI is mostly **flat fills + thick bottom borders**, not parchment-style gradients.

### Hard avoid

- Lime/chartreuse near `#58CC02` / `#89E219`
- Bright sky CTA blue near `#1CB0F6`
- White paper + chunky green CTA combo
- Neon gamification stack as the whole system (green / red / yellow / orange / purple)
- Flat “sticker” game chrome with solid 3–4px lips in those hues

Using **soft gradients** already differentiates us—as long as hues stay off that list.

## Chosen direction: Manuscript (keep)

Current tokens in `frontend/src/app/globals.css`:

- Dark parchment bg `#121a1f`
- Brand gold `#c4a35a` / deep `#8f7435`
- Accent teal `#3f9f84` / deep `#2d7a64`
- Soft coral danger, rose energy, cool gem, warm streak (not Duo Macaw/Cardinal/Bee)

**Verdict:** stay on Manuscript. One global brand; unit themes rotate muted gradients later.

## Layout rules (not just colour)

Colour alone is not enough distance. This document originally covered only hues,
and a Duolingo-shaped **layout** was built in Manuscript colours without breaking
a single rule above — so the rules now cover silhouette too.

`docs/references/` is a folder of **competitor screenshots kept for UX study**:
what information a learner needs at each step, how progress is signalled, how a
lesson is paced. They are **not** visual targets. Copy the questions they answer,
not the shapes they answer them with.

### Hard avoid

| Pattern | Why |
|---------|-----|
| Serpentine / winding node trail | The single most recognisable thing about their path screen |
| Pictogram buttons as the primary node (star, chest, character face) | Their node taxonomy, rendered as their iconography |
| Circular buttons with a thick solid bottom lip | Their button signature, in any hue |
| A mascot character anywhere in the chrome | Duo is their trade dress, and the closest thing to an actual legal risk |
| Full-bleed celebratory confetti / streak-flame theatrics | Reads as the same product with a repaint |

### Chosen direction: the ruled column, as a codex

The path is a **manuscript column**, not a game map:

- One straight ruled line down the margin; lessons hang off it in order
- Nodes are **numbered seals** — a roundel with the chapter number in the brand
  serif, not a pictogram
- The rule is gold above the learner's position and plain below it, so progress
  is legible without a progress bar
- The node's kind (Treasure, Practice) is stated **in words**, not as an icon
- No horizontal offsets — a page has margins, not switchbacks

Implemented as `.path-list` / `.path-item` / `.path-seal` in `globals.css`.

#### It has to survive a hundred lessons

The column alone does not scale: enumerating every lesson of every unit made the
page grow ~647px per unit forever, and left the learner's own lesson buried in a
wall of locked grey rows. A manuscript already answers this — it has a **contents
page**. So the path is a codex:

- A **bookmark** at the top, showing the lesson the learner is on. Fixed height,
  so the one thing a returning learner needs never moves down the page.
- Units are **one line each** in a contents list; only the unit the learner is in
  is expanded. A collapsed unit costs ~69px instead of ~647px, so page height
  stays roughly flat as the curriculum grows. Any unit can be opened by hand.
- Chapters are numbered in **Roman** on the seal and lessons in **Arabic**, so a
  glance tells you which of the two a row is.
- The unit gradient (`--unit-from` / `--unit-to`) lives on the **chapter seal**.
- The rule runs unbroken through *everything* — chapter lines, descriptions, and
  lesson rows alike. It is drawn as one `::before` segment per row rather than a
  single full-height element, so the gold/plain break lands exactly at the
  learner's row whichever chapters happen to be open. **Nothing between the first
  and last row may use `margin` or grid `gap`**: every pixel has to belong to a
  row that carries a segment, or the rule breaks. Use padding instead.

Implemented as `.contents-list` / `.unit-row` / `.unit-open` / `.bookmark`, with
the position logic in `features/learning-path/pathModel.ts` (covered by tests).

### The test to apply

Screenshot the page, replace every colour with greyscale, and ask whether it is
still recognisable as Duolingo. If yes, the silhouette is doing the copying and
the palette is not going to save it.

---

## Future-proof token model

Keep **one global brand**, add **unit themes** so new lessons don’t invent one-off hexes:

```css
/* fixed app-wide */
--brand, --brand-deep, --accent, --accent-deep
--danger, --energy, --gem, --streak
--bg, --bg-elevated, --text, --muted, --line

/* per unit / chapter family */
--unit-from, --unit-to, --unit-glow
```

Use `linear-gradient(135deg, var(--unit-from), var(--unit-to))` on path headers / chapter seals—not on every chip.

### Unit gradients (Manuscript family)

Every family starts at brand gold `#c4a35a`; only the second stop changes. Lives
in `features/learning-path/unitTheme.ts`, applied to the chapter seal.

| Unit feel | Second stop | Numeral | vs page | Note |
|-----------|-------------|---------|---------|------|
| Intro | `#3f9f84` | 6.56 | 6.10 | teal |
| Story | `#8b5e4b` | 5.35 | 4.97 | clay / umber |
| Script | `#5a7d8c` | 5.77 | 5.37 | dusty ink-blue, not Macaw |
| Vine | `#546331` | 4.91 | 4.57 | olive, well clear of Duo's lime |
| Dusk | `#545d96` | 4.94 | 4.59 | indigo |
| Pomegranate | `#914b54` | 4.97 | 4.62 | muted garnet, not Cardinal |
| Liturgy | `#6b5b8c` | 5.07 | 4.70 | muted plum, not Beetle neon |

Same gold brand forever; only `--unit-*` changes — and that single gold anchor is
load-bearing, not just branding. `Liturgy` used to start from a dimmer gold
(`#a67c52`) and put the numeral at **4.00:1**; the plum was never the problem,
and against `#c4a35a` the same plum reaches 5.07. A dim first stop drags the
midpoint down whatever the second stop is.

**Before adding a family, measure it.** The seal draws the chapter numeral in
near black (`#07130f`), so a gradient is only usable if the numeral clears
**4.5:1 against the gradient's midpoint** — where the glyph actually sits.
Measuring a single stop proves nothing. Check the seal against the page
background too (**≥3:1**) or it stops reading as an object.
`unitTheme.test.ts` enforces both for every family.

Two rejected for being too close to what is already there, not for contrast:
`cedar #376c5f` (2° from Intro's teal) and `lapis #3b6597` (13° from Script).
On a contents page where chapters sit rows apart, near-duplicates read as a
repeat. Keep new families **≥25°** from every existing one.

The rotation alternates warm and cool — Intro, Story, Script, Vine, Dusk,
Pomegranate, Liturgy — so neighbouring chapters never look alike. Seven families
is 35 lessons before a colour repeats.

#### Judging "is this too Duolingo?"

Compare **hue, saturation and lightness together**, never hue alone. By hue
alone our dusty blue sits 1° from Macaw and the golds ~6° from Bee yellow, which
looks alarming and is meaningless: the blue is 22% saturated where Macaw is 92%.
Scored across all three, every family above is at zero risk.

## Alternate options (if we ever pivot)

**Desert dawn** — sand → terracotta → indigo (`#d4a574` brand, `#3d4f7c` accent).  
**Ink & lapis** — copper `#b87333` + lapis `#2f5d8c` on deep navy. Both are non-Duo; Manuscript remains preferred.

## Expansion rules

| Token | Fixed? | Notes |
|-------|--------|--------|
| Brand gold | yes | Never Duo green |
| Correct / primary accent | yes (teal family) | Tint only; don’t swap to lime |
| Path / chapter gradient | per unit | `--unit-from` / `--unit-to` |
| Danger / energy / gems | yes | Soft coral / rose / cool blue — not Cardinal / Beetle / Macaw |
