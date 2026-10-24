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

| Area | What it does |
|---|---|
| Alarms | Multiple alarms, repeat days, labels, exact ringing when locked or silent, gradual volume, backup tone |
| Mom | 4 drama levels, 80 lines (English and Hinglish), special lines for Mondays, weekends, New Year and Diwali |
| Mom's voice | Reads her lines aloud with the phone's text-to-speech (stand-in until recorded voice packs) |
| Styles | Chat (Mom texts you), Comic drama, Smart mix (chat, then comic after 3 snoozes) |
| Wake-up tasks | Walk 30 steps, shake 20 times, or 3 sums; always a way to switch to sums |
| Report card | Snoozes, wake-up time, streak, week, badges, monthly verdict; one-tap share image |
| Habits | No-snooze streak, badges, bedtime reminder, weekend mode (10-minute snoozes) |
| Onboarding | Style with previews, language, first alarm, permissions and a battery guide per phone brand |

## Code map

- `core/` and `mom/`: plain Kotlin (no Android), unit tested: tasks, streaks, styles, battery guide, Mom's lines
- `alarm/`: scheduling, the ringing service, Mom's voice, bedtime reminder, boot receiver
- `data/`: alarms, settings and morning history in SharedPreferences
- `ui/`: Compose screens: `home`, `ring` (chat, comic, tasks, report card), `onboarding`, `setup`, `settings`

## Testing on a phone

Settings › Preview Mom now rings a test alarm straight away (snoozing it just stops it, and it isn't counted in your streak). Check on a Xiaomi, Samsung, Vivo, Oppo or Realme phone with the screen locked, since battery savers are the main risk.

## Tech

- Kotlin, Jetpack Compose (Material 3)
- `AlarmManager.setAlarmClock` for exact, reliable alarms
- Foreground service + full-screen intent for the ringing screen
- Min SDK 26, target SDK 35

## Build

Open the project in Android Studio (Ladybug or newer) and run the `app` configuration, or:

```
./gradlew assembleDebug
./gradlew test        # unit tests for core/ and mom/
```

The Comic style uses the Bangers font under the SIL Open Font License (`licenses/Bangers-OFL.txt`).
