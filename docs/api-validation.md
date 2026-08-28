# Validasi API Sanka — Fase 0

> Tanggal: 27 Aug 2026 • Base: https://www.sankavollerei.web.id

## Hasil cek live (curl)

| Endpoint | Sample slug | Status mentah | Mapping Tamat/Belum | Source default Komiku? |
|---|---|---|---|---|
| GET /comic/terbaru | — | — | — | Belum cek live (TODO) |
| GET /comic/search?q=naruto | naruto | — | — | total 4, url https://komiku.org/?s=naruto → kemungkinan Komiku |
| GET /comic/comic/naruto | naruto | `status` belum terverifikasi eksplisit | heuristic: cari kata Tamat/Completed/Ongoing di field status+description | Perlu verifikasi langsung |
| GET /comic/chapter/naruto-chapter-1 | naruto-chapter-1 | — | — | — |

## Kesimpulan sementara
- Wrapper JSON tidak konsisten (`{status: true, data:[]}` vs `{status:"success"}`) → DTO dibuat toleran (String? + default).
- Field `status` publikasi di detail **belum ada sampel lengkap** di dokumentasi live — fallback derive via `LibraryRepository.parsePublication()` (cari Tamat/Completed/Ongoing di raw+description, default UNKNOWN).
- Thumbnail & chapter images bisa null / tanpa https → DTO nullable + Coil placeholder.
- Rate limit 30/menit/IP — client guard sudah ada, debounce 450ms, cache 10m.

## TODO Fase 0.5
- Jalankan `scripts/validate-api.sh` dengan 5 slug populer (naruto, one_piece, solo_leveling, jujutsu_kaisen, chainsaw_man) dan isi tabel di atas dengan respons nyata.
