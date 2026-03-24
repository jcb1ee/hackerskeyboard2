## Hacker's Keyboard 2

A full-featured soft keyboard for power users and developers on Android.

**Targets Android 5–14 (API 21–34).** Updated from the original [Hacker's Keyboard](https://github.com/klausw/hackerskeyboard) with all Android 12+ compatibility fixes applied.

Are you missing the key layout you're used to from your computer when using an Android device? This software keyboard has separate number keys, punctuation in the usual places, and arrow keys. It supports multitouch for the modifier keys.

This keyboard is especially useful if you use ConnectBot or Termux for SSH access. It provides working Tab/Ctrl/Esc keys, and arrow keys are essential for devices that don't have a trackball or D-Pad.

### Features

- **5-row layout** with dedicated number row
- **Ctrl, Alt, Meta** modifier keys with multi-touch support
- **Arrow and navigation keys** (Tab, Esc, Page Up/Down, Home, End, Insert)
- **Korean (한국어)** Dubeolsik Hangul input with full syllable composition
- **40+ language layouts** — see below
- Plugin dictionary support

### Supported Languages

Armenian (Հայերեն), Arabic (العربية), British (en-GB), Bulgarian (български), Czech (Čeština),
Danish (dansk), Dutch, Finnish (Suomi), French (Français, AZERTY), German (Deutsch, QWERTZ),
German Neo2, Greek (ελληνικά), Hebrew (עברית), Hungarian (Magyar), Italian (Italiano),
Korean (한국어), Lao (ພາສາລາວ), Norwegian (Norsk bokmål), Persian (فارسی), Polish, Portuguese,
Romanian, Russian (Русский, standard and phonetic), Serbian (Српски), Slovak, Slovenian,
Spanish (Español, Latinoamérica), Swedish (Svenska), Tamil (தமிழ்), Thai (ไทย), Turkish, Ukrainian,
Carpalx English (en-CX), Dvorak English (en-DV), and more.

### Building

```bash
./gradlew assembleDebug
```

For a signed release build, set the following environment variables before running `assembleRelease`:

```bash
export KEYSTORE_PATH=/path/to/release.jks
export KEYSTORE_PASSWORD=...
export KEY_ALIAS=hackerskeyboard2
export KEY_PASSWORD=...
./gradlew assembleRelease
```

### License

Apache License 2.0. See [LICENSE](LICENSE).

Based on the AOSP Gingerbread soft keyboard.

![hk-5row-en-s.png](hk-5row-en-s.png)
