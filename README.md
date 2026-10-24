# Like Home

**Wake up like Mom is still calling you.**

Like Home is an Android alarm clock where a loving but dramatic Mom wakes you up. Every snooze makes her more dramatic, and you stop the alarm by proving you're really out of bed.

## Branches

| Branch | Purpose |
|---|---|
| `feature/*` | One branch per feature. Merged into `develop` when done. |
| `develop` | Integration branch. Latest finished features. |
| `release` | Only updated after the owner validates `develop` in the app. |

## Tech

- Kotlin, Jetpack Compose (Material 3)
- `AlarmManager.setAlarmClock` for exact, reliable alarms
- Foreground service + full-screen intent for the ringing screen
- Min SDK 26, target SDK 35

## Build

Open the project in Android Studio (Ladybug or newer) and run the `app` configuration, or:

```
./gradlew assembleDebug
```
