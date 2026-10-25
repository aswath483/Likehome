# Like Home: to-do

What's left, and who it waits on. Tick items off as they're done.

## Waiting on the owner

- [ ] **Firebase for live family mode** (code is done and hidden until then; see
      `docs/family-live-setup.md`)
  - [ ] Create the Firebase project, add the Android app `com.likehome.app`
  - [ ] Enable Anonymous sign-in and create Firestore
  - [ ] Upgrade to the Blaze plan (needed for Cloud Functions)
  - [ ] Add `google-services.json` to `app/`
  - [ ] Add GitHub secrets `FIREBASE_PROJECT_ID` and `FIREBASE_SERVICE_ACCOUNT`, then run
        **Deploy family backend** from the Actions tab
  - [ ] Test on two phones: pair, set an alarm, snooze, WAKE UP!, unlink
- [ ] **Paid tier decision**: everything free, an optional tip, or premium features
- [ ] **Release signing key** (needed for the Play Store upload)
- [ ] **Contact email** in `docs/privacy.html` (still says "[contact email]")
- [ ] **Test on a real phone**: alarms over the lock screen, snooze, each wake-up task,
      sleep sounds, calendar wake-up, backup and restore, WhatsApp voice note sharing
- [ ] **Test with a watch**: Snooze from the wrist
- [ ] **Native speakers review** each language (Spanish, Portuguese, French, German,
      Arabic, Indonesian, Hindi), especially Mom's lines

## Launch prep (can be built now)

- [ ] Auto-generated screenshots of every screen in all 8 languages (also catches text
      overflow and right-to-left layout problems)
- [ ] Signed release build in CI
- [ ] Play Store listing text in all 8 languages
- [ ] Accessibility pass: screen reader labels, large fonts, touch targets

## Done recently

- [x] App in 8 languages, Mom speaks 9
- [x] Alarm sound per alarm, calendar-aware wake-up, sleep sounds
- [x] Backup and restore
- [x] Watch: Snooze from the wrist
- [x] Mom's real voice from WhatsApp, "Tell Mom I'm up"
- [x] Live family mode code (waiting on Firebase above)
