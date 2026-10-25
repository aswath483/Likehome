# Like Home

**Wake up like Mom is still calling you.**

Like Home is an Android alarm clock where a loving but dramatic Mom wakes you up. Every snooze makes her more dramatic, and you stop the alarm by proving you're really out of bed.

## Branches

| Branch | Purpose |
|---|---|
| `feature/*` | One branch per feature. Merged into `develop` when done. |
| `develop` | Integration branch. Latest finished features. |
| `release` | Only updated after the owner validates `develop` in the app. |

## What's in the app

Everything is included; there is no paid tier.

| Area | What it does |
|---|---|
| Alarms | Multiple alarms, repeat days, labels, exact ringing over the lock screen, gradual volume, volume floor, backup tone, snoozes that survive reboots |
| Characters | Mummy, Papa, Dadi, the Boss, Abuela: own lines at 4 drama levels and their own faces |
| Languages | English, Hinglish and Hindi (Devanagari); festival lines via Remote Config |
| Styles | Chat, Comic drama, Breaking news, Retro game, Filmy drama, Warm kitchen, Calm night, Fridge notes, Smart mix |
| Wake-up tasks | Walk 30 steps, shake 20 times, 3 sums, photo check, say it out loud, type a promise |
| Voices | Record your own voice per level; bundled voice packs (`docs/voice-packs.md`); phone text-to-speech fallback |
| Report card | Snoozes, wake-up time, streak, week, badges, monthly verdict; share image; WhatsApp stickers |
| Habits | Streaks and calendar, bedtime / water / breakfast reminders, weekend mode |
| Extras | Home screen widget, power nap Quick Settings tile, app shortcuts, 12/24-hour clocks |
| Onboarding | Character, style, language, first alarm, permissions and a battery guide per phone brand |

## Code map

- `core/` and `mom/`: plain Kotlin (no Android), unit tested: tasks, streaks, styles, battery guide, Mom's lines
- `alarm/`: scheduling, the ringing service, Mom's voice, bedtime reminder, boot receiver
- `data/`: alarms, settings and morning history in SharedPreferences
- `ui/`: Compose screens: `home`, `ring` (chat, comic, tasks, report card), `onboarding`, `setup`, `settings`, `history`, `styles`, `premium`, `about`
- `ui/nav/`: a small back stack (survives rotation, keeps scroll position, direction-aware slides, system Back)
- `ui/common/`: the design kit: page scaffold with insets, buttons, cards, pills, press-scale and haptics

## Testing on a phone

Settings › Preview Mom now rings a test alarm straight away (snoozing it just stops it, and it isn't counted in your streak). Check on a Xiaomi, Samsung, Vivo, Oppo or Realme phone with the screen locked, since battery savers are the main risk.

## Tech

- Kotlin, Jetpack Compose (Material 3)
- `AlarmManager.setAlarmClock` for exact, reliable alarms
- Foreground service + full-screen intent for the ringing screen
- Min SDK 26, target SDK 35

## Firebase (optional)

The app builds and runs without Firebase. To turn on Analytics, Crashlytics and Remote
Config, add your project's `google-services.json` to `app/`. Remote Config key
`festival_lines` takes a JSON list such as
`[{"date":"2027-10-29","english":"Happy Diwali, beta!","hinglish":"…","hindi":"…"}]`.

## Launch material

`docs/` holds the landing page and privacy policy (ready for GitHub Pages: Settings ›
Pages › branch `develop`, folder `/docs`) and `docs/play-store-listing.md` with the store
text, permission declarations, data safety answers and screenshot list.

## Build

Open the project in Android Studio (Ladybug or newer) and run the `app` configuration, or:

```
./gradlew assembleDebug
./gradlew test        # unit tests for core/ and mom/
```

Fonts: DM Serif Display (headlines and clocks), Hind (text, with Devanagari for later Hindi lines) and Bangers (Comic style), all under the SIL Open Font License (`licenses/`).
