# Zomic — Manga Reader + Auto Library Organizer

> **Package:** `com.zaaamzomic` • **MinSdk 26** • **Target 35** • **Repo:** https://github.com/az925-crypto/Zomic

Manga reader Android yang auto-tag status publikasi (Tamat/Belum Tamat dari API Sanka) + bookmark manual (Sedang/Belum/Dropped). Library bisa difilter 2 dimensi, lanjut baca <3 tap.

## Mockup

Mockup HTML full-app ada di `mockups/index.html` — jalan di `http://127.0.0.1:8000` (gate sebelum masuk Compose, sudah di-approve via Telegram).

```
python3 -m http.server 8000 --directory mockups
```

## Build

Build berat wajib di GitHub Actions (note.txt):

- **CI:** `.github/workflows/build.yml` — `testReleaseUnitTest` → `assembleRelease` → upload artifact + Release saat tag `v*`.
- **Lokal (ringan):** `./gradlew assembleDebug` (optional, tidak wajib).

Signing: set `secrets.KEYSTORE_BASE64` + `KEYSTORE_PASSWORD` + `KEY_ALIAS` + `KEY_PASSWORD` — fallback debug jika tidak ada.

## Arsitektur

- **app:** single module (Compose, Navigation, Room, Retrofit, Coil)
- **data/network:** `SankaComicService` + `RateLimitInterceptor` (token bucket 30/menit, block mangasusuku/nekopoi, 429 retry)
- **data/db:** `ZomicDatabase` v1 (`manga_library`) + `LibraryRepository`
- **ui:** `Terbaru / Cari / Detail / Reader / Library` (Spine Strip signature, Paper Ivory + Sumi + Hanko)

## API

Base `https://www.sankavollerei.web.id` — `GET /comic/terbaru`, `/comic/search?q=`, `/comic/comic/:slug`, `/comic/chapter/:slug`. No key wajib, patuh 30/menit.

## Status

**MVP FR-1 → FR-10 SELESAI 100%** — mockup APPROVED, Compose 5 screen, Room v1, Retrofit + rate-limit 30/menit, Coil 250MB, blocking security/performance/bug clear. Build release via Actions → artifact `Zomic-v1.0.0-release.apk` + `.sha256` di Releases (tag `v*`). Validasi API di `docs/api-validation.md`, keputusan di `docs/decisions.md`.

Cara rilis: `git tag v1.0.0 && git push origin v1.0.0` → Actions attach APK ke GitHub Release.
