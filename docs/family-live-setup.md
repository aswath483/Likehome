# Live family mode: setup

Live family mode lets a parent set their child's alarms from their own phone, see how
the morning went, and ring them with "WAKE UP!". The code is all in the repo; it switches
on when the app is built with a Firebase config file. Until then the feature is hidden.

## What's where

| Part | File |
|---|---|
| App side | `app/src/main/java/com/likehome/app/family/` and `ui/family/FamilyLiveScreen.kt` |
| Security rules | `firestore.rules` |
| Server functions | `functions/index.js` (tests: `functions/test/`) |
| Deploy | `.github/workflows/firebase-deploy.yml` |

## One-time setup

1. **Create the project** at console.firebase.google.com.
2. **Add an Android app** with package name `com.likehome.app`, download
   `google-services.json` and put it in `app/` (next to `build.gradle.kts`).
3. **Authentication › Sign-in method:** enable **Anonymous**.
4. **Firestore Database:** create it in production mode, in the region nearest your users.
5. **Upgrade to the Blaze plan** (Cloud Functions need it; small apps usually stay free).
6. **Deploy the rules and functions.** Either:
   - from the repo's Actions tab, run **Deploy family backend** after adding the secrets
     `FIREBASE_PROJECT_ID` and `FIREBASE_SERVICE_ACCOUNT` (Project settings › Service
     accounts › Generate new private key), or
   - on a computer: `npm i -g firebase-tools`, `firebase login`,
     `firebase deploy --only firestore,functions --project <project-id>`.

## How it works

- The child taps **I'm the one waking up** and gets a 6-digit code, valid 10 minutes.
- The parent taps **I'm the parent** and types it; the `joinFamily` function checks the
  code and adds them to the child's family.
- Alarms the parent saves go to `families/{id}/alarms`; `onAlarmChange` pushes the child's
  phone, which syncs and arms them. They show on the child's Home as "Set by family".
- The child's phone writes `status/now` when the alarm rings, is snoozed, answered or
  missed; `onStatus` pushes the parents, whose phones notify in their own language.
- **WAKE UP!** writes to `pokes`; `onPoke` pushes the child's phone, which rings Mom at
  full drama.
- Either side can unlink. The rules only let family members read or write a family, and
  nobody can add themselves to one without a valid code.
