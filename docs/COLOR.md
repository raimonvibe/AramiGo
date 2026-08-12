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

### Example unit gradients (Manuscript family)

| Unit feel | Gradient |
|-----------|----------|
| Intro | `#c4a35a → #3f9f84` (current) |
| Script | `#b8956a → #5a7d8c` (dusty ink-blue, not Macaw) |
| Liturgy | `#a67c52 → #6b5b8c` (muted plum, not Beetle neon) |
| Story | `#c4a35a → #8b5e4b` (clay/umber) |

Same gold brand forever; only `--unit-*` changes.

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
