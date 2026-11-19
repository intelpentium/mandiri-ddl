# Movie Guide

Aplikasi Android untuk menjelajahi film dari TMDB, membuka trailer dan ulasan, serta menyimpan film favorit.

## Cara menjalankan

1. Buka proyek di Android Studio. Gunakan JDK 21 dan Android SDK 36.
2. Ambil **API Read Access Token** dari [pengaturan API TMDB](https://www.themoviedb.org/settings/api). API Key v3 juga bisa dipakai.
3. Tambahkan baris ini ke `local.properties` di folder utama proyek. Biarkan baris `sdk.dir` yang sudah ada.

   ```properties
   TMDB_READ_ACCESS_TOKEN=isi_dengan_token_anda
   ```

4. Pilih konfigurasi `app`, lalu klik **Run** di emulator atau perangkat Android API 24 ke atas.

Gunakan token sendiri saat build. Token masuk ke APK, jadi bagikan source code untuk penilaian.

## Tes dan build

```bash
./gradlew :data:testDebugUnitTest :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:bundleRelease
```

APK debug ada di `app/build/outputs/apk/debug/app-debug.apk`.

This product uses the TMDB API but is not endorsed or certified by TMDB.
