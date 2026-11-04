# Kanji-Learn

Aplikasi Android untuk belajar kanji dan kotoba JLPT N5–N4, lengkap dengan widget, stroke order, latihan menulis, dan sistem review bergaya Anki (SRS).

## Fitur

- **Widget 4x2** — tampil kanji/kotoba bergantian otomatis tiap 4 jam, tema warm dark, tombol refresh & skip
- **Stroke order** — animasi urutan goresan + mode latihan menulis (trace) untuk 253 kanji
- **Anki / SRS** — review dengan rating Again/Hard/Good/Easy, algoritma SM-2, jadwal tersimpan antar sesi
- **TTS Jepang** — dengar pengucapan kata dan kalimat contoh
- **Browse & search** — 256 kanji + 202 kotoba, filter N5/N4/Ekstra, grid aesthetic warm dark
- **Detail substantif** — on/kun reading, arti, contoh kalimat, stroke order

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Glance untuk App Widget
- Room-free persistence: SharedPreferences untuk state SRS
- Dataset lokal di `app/src/main/assets/` — tanpa server, offline-first
- Min SDK 26, Target SDK 35

## Struktur

```
app/src/main/java/com/amr/kanjiwidget/
├── data/          Models, repository, SRS store (SM-2)
├── stroke/        SVG path parser + stroke order canvas
├── tts/           Japanese text-to-speech
├── ui/            Compose screens (beranda, kanji, kotoba, anki, detail)
├── widget/        Glance app widget
└── MainActivity.kt
```

## Build

Butuh JDK 21 dan Android SDK (build-tools 35, platform android-35).

```bash
export GRADLE_USER_HOME=/path/to/gradle-home
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Dataset

- `assets/kanji.json` — 256 kanji (N5/N4/ekstra)
- `assets/kotoba.json` — 202 kotoba bertema
- `assets/strokes.json` — 253 kanji, 2425 path stroke order
