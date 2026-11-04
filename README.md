# Kanji-Learn

Aplikasi Android buat belajar kanji & kotoba JLPT N5–N4. Offline, semua data ada di dalam APK, tanpa server.

## Download & Install

APK-nya ada di [`APK/KanjiWidget.apk`](APK/KanjiWidget.apk). Download, buka file-nya, install.

Karena APK ini bukan dari Play Store, Android bakal minta izin *install dari sumber tidak dikenal*. Izinkan, lanjut install, selesai.

## Isi aplikasi

- Widget 4x2: kanji/kotoba berganti otomatis tiap 4 jam, ada tombol refresh & skip
- Stroke order: animasi urutan goresan + mode latihan menulis
- Review ala Anki: rating Again/Hard/Good/Easy, algoritma SM-2, jadwal tersimpan
- TTS Jepang: dengar pengucapan kata & kalimat contoh
- 256 kanji + 202 kotoba, bisa search dan filter N5/N4/Ekstra

## Build sendiri

Butuh JDK 21 dan Android SDK 35.

```bash
./gradlew assembleDebug
```

Hasilnya di `app/build/outputs/apk/debug/app-debug.apk`.
