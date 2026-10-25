# Voice packs

When the alarm rings, each character's voice comes from the first of these that exists:

1. **The user's own recording** for that drama level (Settings › Record a voice).
2. **A bundled voice pack clip** in the app's assets.
3. **The phone's text-to-speech**, reading the line on screen.

## Adding a pack

Put audio files (`.mp3`, `.ogg` or `.m4a`) in:

```
app/src/main/assets/voices/<character>/<language>/<level>/
```

- `<character>`: `mom`, `papa`, `dadi`, `boss`, `abuela`
- `<language>`: `english`, `hinglish`, `hindi`, `spanish`, `portuguese`, `french`, `german`, `arabic`, `indonesian`
- `<level>`: `0` sweet, `1` annoyed, `2` comparing, `3` full drama

Any number of clips per folder; one is picked per alarm. Keep clips under 15 seconds and
normalise loudness so levels sound the same volume. Example:

```
assets/voices/mom/hinglish/0/good_morning_beta.mp3
assets/voices/mom/hinglish/3/balti_la_rahi_hoon.mp3
```

A voice actor script is simply the lines in `mom/MomLines.kt` and `mom/CharacterLines.kt`.
