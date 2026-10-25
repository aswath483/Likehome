# Character art brief

Portraits for the five characters, one per drama level: **20 images**, plus 60 more
for the extra looks of Mummy, Papa and the Boss (see below). The app
shows them everywhere a character appears (ringing screens, gallery, onboarding, report
card, share image). It shows the drawn faces until the files exist, so they can arrive
one at a time.

## Deliverables

| | |
|---|---|
| Count | 5 characters × 4 moods = 20, plus 3 characters × 5 extra looks × 4 moods = 60 |
| Size | 1024 × 1024 px, square |
| Format | WebP (quality 75, about 35 KB) or PNG |
| Folder | `app/src/main/assets/characters/<character>/<level>.webp` |
| Characters | `mom`, `papa`, `dadi`, `boss`, `abuela` |
| Levels | `0` sweet · `1` annoyed · `2` comparing · `3` full drama |

Example: `assets/characters/mom/3.webp` is Mummy at full drama.

**Framing.** The app crops every image to a **circle**. Head and shoulders, face centred,
eyes about a third of the way down, everything important inside the middle 80%. Plain,
soft, warm background with no text, logos or other people. The same camera angle, light
and background for all four moods of a character, so changing mood looks like the same
moment.

## Looks for a global audience

Mummy, Papa and the Boss come in six looks the user picks in **Settings › Styles &
characters**: South Asian, East Asian, African, European, Latin American and Middle
Eastern. The character's default look lives in its own folder; every other look has a
subfolder with the same four files:

```
assets/characters/mom/0-3.webp             South Asian (default)
assets/characters/mom/east_asian/0-3.webp
assets/characters/boss/0-3.webp            African (default)
assets/characters/boss/south_asian/0-3.webp
```

Folder names: `south_asian`, `east_asian`, `african`, `european`, `latin`,
`middle_eastern`. Dadi and Abuela belong to one culture by design and have one look.
`tools/generate_portraits.py` holds the prompts for every look (`--looks` makes any that
are missing).

## Pick one art style and keep it

The shipped set is **3D animated** (`PORTRAIT_STYLE=animated`, the script's default).
Other options considered:

- **Warm photographic.** Natural light, shallow depth of field, real skin texture, gentle
  film grade. The most "home", and closest to real users' own photos.
- **Painted semi-real.** A digital painting with soft brushwork, realistic proportions
  and warm colours. Ages well, sits nicely next to the app's own design, and avoids
  uncanny AI faces.

## The cast

**Mummy (`mom`)**: Indian woman, late 40s, warm brown skin, kind eyes, black hair in a
low bun with a little grey, a small red bindi, a thin gold chain, a soft cotton saree or
kurta in a warm colour. Background: a sunlit home kitchen, blurred.

**Papa (`papa`)**: Indian man, mid-50s, short greying hair, a neat moustache,
rectangular reading glasses, a pressed half-sleeve checked shirt. Background: a living
room with morning light, blurred.

**Dadi (`dadi`)**: Indian grandmother, mid-70s, silver hair in a bun, round
gold-rimmed glasses, a cream or white cotton saree with a coloured border, a shawl on
her shoulders. Background: a courtyard or a window with plants, blurred.

**The Boss (`boss`)**: an office manager in their 40s (your choice of a man or a
woman), smart blazer, lanyard, a coffee mug in hand, confident. Background: a bright
modern office, blurred.

**Abuela (`abuela`)**: Latin American grandmother, early 70s, short curly silver hair,
small gold hoop earrings, a floral blouse with an apron. Background: a warm, colourful
kitchen, blurred.

## The four moods

| Level | Mood | Expression and pose |
|---|---|---|
| 0 | Sweet | Warm, soft smile, relaxed eyes, maybe holding a cup of chai. Pure affection. |
| 1 | Annoyed | Raised eyebrows, lips pressed, one hand on hip. "I'm not calling again." |
| 2 | Comparing | Side-eye, head tilted, a sceptical half-smile, maybe pointing at a phone. "Sharma ji's son…" |
| 3 | Full drama | Theatrical outrage: wide eyes, mouth open mid-shout, hands up. Mummy holds a steel bucket, Papa a TV remote, Dadi a walking stick, the Boss a laptop, Abuela a slipper. Funny, never frightening. |

The characters must always read as **loving**. Full drama is a comedy beat, not anger
at the viewer: no clenched fists aimed at the camera and no real threat.

## Prompts

Generate **level 0 first** for each character. Then keep that same person for the other
three moods:
- **Midjourney:** add the level 0 image as a character reference (`--cref <url> --cw 100`).
- **Tools with image editing (Ideogram, Flux, GPT image):** upload the level 0 image and ask for "the same person, same clothes, background and light, only change the expression to …".
- **Stable Diffusion:** train a small LoRA on the chosen face.

Template (warm photographic):

> Warm natural-light portrait photograph of **[character description]**, head and
> shoulders, centred, looking at the camera, **[mood expression]**, soft blurred
> **[background]**, shallow depth of field, gentle film colour grade, square 1:1, plain
> composition, no text

Template (painted semi-real):

> Semi-realistic digital painting portrait of **[character description]**, head and
> shoulders, centred, looking at the viewer, **[mood expression]**, soft blurred
> **[background]**, warm palette, soft brushwork, square 1:1, no text

Ready-made, photographic (swap the first words for the painted look):

1. `mom/0`: Warm natural-light portrait photograph of an Indian woman in her late 40s with warm brown skin, kind eyes, black hair in a low bun with a little grey, a small red bindi, a thin gold chain and a soft orange cotton saree, smiling warmly while holding a cup of chai, soft blurred sunlit home kitchen, shallow depth of field, square 1:1, no text
2. `mom/1`: same woman, eyebrows raised and lips pressed together, one hand on her hip, mildly annoyed
3. `mom/2`: same woman, side-eye glance, head tilted, sceptical half-smile, pointing at a phone screen
4. `mom/3`: same woman, theatrical comic outrage, eyes wide, mouth open mid-shout, holding up a steel bucket of water, funny and loving
5. `papa/0`: Warm natural-light portrait photograph of an Indian man in his mid-50s with short greying hair, a neat moustache, rectangular reading glasses and a pressed blue checked half-sleeve shirt, a small calm smile, soft blurred living room in morning light, shallow depth of field, square 1:1, no text
6. `papa/1`: same man, looking over his glasses, unimpressed, arms folded
7. `papa/2`: same man, eyebrow raised, holding up a folded newspaper, quietly comparing
8. `papa/3`: same man, dramatic disappointment, hand on forehead, holding a TV remote out of reach, comic
9. `dadi/0`: Warm natural-light portrait photograph of an Indian grandmother in her mid-70s with silver hair in a bun, round gold-rimmed glasses, a cream cotton saree with a maroon border and a shawl, a gentle loving smile, soft blurred courtyard with plants, shallow depth of field, square 1:1, no text
10. `dadi/1`: same grandmother, peering over her glasses, gently scolding, a finger raised
11. `dadi/2`: same grandmother, eyes narrowed with a knowing look, telling an old story
12. `dadi/3`: same grandmother, comic theatrical shock, both hands raised, walking stick in one hand, "Hai Ram!" expression, loving
13. `boss/0`: Warm natural-light portrait photograph of an office manager in their 40s in a smart navy blazer with a lanyard, holding a coffee mug, a friendly confident smile, soft blurred bright modern office, shallow depth of field, square 1:1, no text
14. `boss/1`: same person, polite tight smile, tapping a wristwatch
15. `boss/2`: same person, looking at a laptop then at the camera, eyebrow raised, unimpressed
16. `boss/3`: same person, mock-panic, holding a laptop up showing an urgent calendar invite, comic
17. `abuela/0`: Warm natural-light portrait photograph of a Latin American grandmother in her early 70s with short curly silver hair, small gold hoop earrings, a floral blouse and an apron, a big warm smile, soft blurred colourful kitchen, shallow depth of field, square 1:1, no text
18. `abuela/1`: same grandmother, hands on hips, eyebrows raised, "ay, mijo" look
19. `abuela/2`: same grandmother, side-eye with a sceptical smile, wagging a finger
20. `abuela/3`: same grandmother, comic outrage, holding up a slipper (chancla), eyes wide, funny and loving

## Rules

- **No real people.** Never prompt with a celebrity's or real person's name or likeness.
- **Respectful.** Natural, everyday people, not ethnic caricatures or exaggerated features.
- **Commercial rights.** Use a tool or illustrator whose terms allow commercial use in an app, and keep the prompts and the receipts or contract.
- **Consistency over variety.** Pick the best set, not the best single image.

## Checking in the app

1. Save the files into the folders above.
2. Build the app; no code changes are needed.
3. Settings › Styles & characters › Preview each character and style.
4. Check the circle crop in the gallery, the ringing screens and the share image.

Users can still put their own Mom's photo on any character (Settings › Use your own
photos); their photos always win over the built-in art.
