# PRD: [Nama Aplikasi — TBD]
### Manga Reader dengan Auto Library Organizer

**Versi:** 1.0
**Tanggal:** 27 Agustus 2026
**Status:** Draft — siap direview / di-feed ke planner agent
**Sumber data:** Sanka Vollerei API — `https://www.sankavollerei.web.id` (verifikasi dokumentasi 27 Agustus 2026, repo: `github.com/SankaVollereii/Rest-Api-Anime-and-Comic`, lisensi MIT)

---

## 1. Ringkasan Eksekutif

Aplikasi Android untuk baca manga/komik yang punya sistem "auto library organizer" — manga yang dibaca otomatis kegolong berdasarkan status publikasi (Tamat/Belum Tamat, disinkron dari data API), sekaligus bisa di-bookmark manual berdasarkan progres baca user (Sedang Dibaca/Belum Dibaca/Dropped). Konten manga diambil dari API Sanka Vollerei, gratis tanpa API key wajib.

---

## 2. Masalah

User yang follow banyak judul manga sekaligus kesulitan tracking: mana yang lagi dibaca, mana yang di-drop, mana yang serinya udah tamat tapi belum kelar dibaca. Aplikasi baca manga kebanyakan cuma nampilin daftar chapter tanpa sistem organizer bacaan yang jelas.

**User story:**
Pembaca manga yang follow banyak judul sekaligus, butuh cara cepat buat lihat "yang mana yang harus gue lanjutin baca" tanpa harus scroll ulang riwayat atau inget-inget manual judul apa aja yang masih ongoing vs udah tamat.

---

## 3. Target User

- Pembaca manga/komik yang aktif follow banyak judul
- User yang pernah pakai app tracking semacam MyAnimeList/AniList tapi mau fitur itu nyatu langsung sama reader-nya

**Bukan target (v1):**
- User yang butuh multi-source sejak awal (MVP fokus 1 source default)
- Konten 18+ (source Mangasusuku di-exclude dari MVP, lihat Section 10)

---

## 4. Tujuan & Metrik Sukses

| Tujuan | Metrik | Target |
|---|---|---|
| Cepat lanjut baca | Tap dari buka app ke halaman terakhir dibaca | < 3 tap |
| Auto-tag akurat | Status publikasi manga match data API | 100% |
| Reading experience smooth | Waktu load per halaman chapter (koneksi normal) | < 2 detik |
| Retention untuk seri ongoing | User buka ulang manga status "Sedang Dibaca" | ≥ 3x/minggu |
| Patuh rate limit | Request per user session ke API | < 30/menit |

---

## 5. Scope & Prioritas

### MVP (v1)

| Fitur | Prioritas |
|---|---|
| Search manga (via API) | P0 |
| Browse manga terbaru (via API) | P0 |
| Baca chapter (image viewer) | P0 |
| Bookmark reading status: Sedang Dibaca / Belum Dibaca / Dropped | P0 |
| Auto-tag status publikasi: Tamat / Belum Tamat (dari API) | P0 |
| Library screen dengan filter status | P0 |
| Simpan posisi baca terakhir per manga | P1 |

### Phase 2 — nice-to-have

| Fitur | Prioritas |
|---|---|
| Notifikasi chapter baru untuk manga "Sedang Dibaca" | P1/P2 |
| Download chapter untuk baca offline | P2 |
| Multi-source (tambah Shinigami/Komikstation sebagai opsi) | P2 |
| Custom reading mode (webtoon scroll vs page-by-page) | P2 |

### Eksplisit di luar scope
- Upload/hosting konten sendiri — app ini murni reader/consumer
- Fitur sosial (komentar, rating, share) di v1
- Sync progress lintas akun/device
- Source berkonten 18+ (Mangasusuku) — kecuali diputuskan lain di Keputusan Terbuka #3

---

## 6. Functional Requirements

**Browse & Discovery**
- **FR-1:** User bisa search manga by judul — `GET /comic/search?q={keyword}`
- **FR-2:** User bisa browse manga terbaru — `GET /comic/terbaru`
- **FR-3:** User bisa lihat detail manga (sinopsis, status publikasi, daftar chapter) — `GET /comic/comic/:slug`

**Reading**
- **FR-4:** User bisa baca chapter dengan image viewer (swipe/scroll) — `GET /comic/chapter/:slug`
- **FR-5:** App menyimpan posisi baca terakhir (chapter + halaman) per manga
- **FR-6:** User bisa lanjut baca dari posisi terakhir langsung dari Library

**Library / Auto Organizer**
- **FR-7:** User bisa bookmark manga ke salah satu reading status: Sedang Dibaca, Belum Dibaca, Dropped
- **FR-8:** App otomatis menandai status publikasi (Tamat/Belum Tamat) berdasarkan data dari respons detail API — tanpa input manual
- **FR-9:** Library screen bisa difilter berdasarkan reading status dan/atau status publikasi
- **FR-10:** User bisa pindahin manga antar reading status kapan saja

**Update (Phase 2)**
- **FR-11:** App cek chapter baru untuk manga berstatus "Sedang Dibaca"
- **FR-12:** User dapat notifikasi saat ada chapter baru

---

## 7. Alur Utama (User Flows)

**Alur baca & bookmark**
1. User search/browse manga dari halaman utama
2. Tap manga → lihat detail (sinopsis, status publikasi, list chapter)
3. Tap chapter → masuk reader, baca
4. User bookmark manga ke reading status (dari halaman detail atau reader)
5. Manga otomatis ke-tag status publikasinya dari data API, tampil di card Library

**Alur lanjut baca**
1. Buka app → tab Library, filter "Sedang Dibaca"
2. Tap manga → app langsung lompat ke chapter/halaman terakhir dibaca

---

## 8. Non-Functional Requirements

- **Performa:** image caching biar chapter yang sudah dibuka tidak perlu re-download
- **Reliability:** kalau API down/error, tampilkan pesan jelas + tetap bisa akses Library dari cache lokal
- **Rate limit compliance:** API membatasi **30 request/menit/IP** — app wajib debounce search input, cache response, dan hindari batch-fetch (misal jangan load semua thumbnail sekaligus tanpa jeda)
- **Offline parsial:** Library & riwayat baca tetap bisa diakses offline; baca chapter baru butuh koneksi (kecuali fitur download Phase 2 sudah ada)
- **Compatibility:** target Android 8 (API 26) ke atas

---

## 9. Arsitektur Teknis

**Stack:** Kotlin + Jetpack Compose, Room (local library & reading history), Retrofit/Ktor untuk konsumsi API

| Modul | Tanggung Jawab |
|---|---|
| `core-network` | Konsumsi API Sanka Vollerei — search, browse, detail, chapter — dengan caching & rate-limit guard bawaan |
| `core-library` | Room DB — reading status, riwayat baca, sinkron status publikasi dari API |
| `feature-reader` | Image viewer, reading mode, cache gambar |
| `feature-browse` | UI search & discovery |

**Pemetaan endpoint (`core-network`):**

| Kebutuhan | Endpoint |
|---|---|
| Search manga | `GET /comic/search?q={keyword}` |
| Manga terbaru | `GET /comic/terbaru` |
| Detail manga | `GET /comic/comic/:slug` |
| Baca chapter | `GET /comic/chapter/:slug` |

**Alur data:** `core-network` fetch data manga & chapter dari API → `feature-browse`/`feature-reader` render → user action (bookmark, progres baca) disimpan ke `core-library` (Room) → status publikasi di `core-library` disinkron ulang tiap kali `core-network` fetch detail manga terbaru.

> Catatan: endpoint default (`/comic/search`, `/comic/comic/:slug`, `/comic/chapter/:slug`) kemungkinan besar bersumber dari **Komiku**, berdasarkan URL yang muncul di contoh respons dokumentasi. Perlu dikonfirmasi langsung — lihat Keputusan Terbuka #2.

---

## 10. Keamanan & Ketergantungan Eksternal

**Autentikasi & rate limit**
- API key tidak wajib untuk akses dasar (ada FREE APIKEY opsional `planaai` di `/docs`, tidak wajib dipakai)
- Rate limit: **30 request/menit/IP** — 3x pelanggaran berujung **ban permanen per IP** (unban gratis via kontak owner, tapi tetap merusak UX kalau kejadian)
- Implikasi desain: `core-network` wajib punya request queue/debounce dari awal, bukan ditambah belakangan

**Ketergantungan API pihak ketiga**
- Tidak ada versioning eksplisit — breaking change bisa terjadi tanpa pemberitahuan (diakui langsung oleh dokumentasi API-nya)
- Sebagian endpoint diakui belum stabil oleh pihak API sendiri (disebutkan: Kuramanime, sebagian Komikstation, beberapa filter) — endpoint comic default yang dipakai MVP di luar daftar ini
- Lisensi API: MIT, gratis untuk personal & komersial — aman dari sisi lisensi pemakaian API itu sendiri

**Konten**
- API bersifat scraping-based dari situs publik (Komiku, BacaKomik, Komikstation, dll). Dokumentasi API sendiri secara eksplisit mengingatkan untuk menghormati hak cipta & ToS sumber asli — ini relevan untuk keputusan jalur distribusi (Play Store vs sideload)
- Salah satu source comic (**Mangasusuku**) berisi konten 18+. **Di-exclude dari MVP** — app hanya konsumsi endpoint comic default, tidak menyentuh `/comic/mangasusuku/*`

---

## 11. Edge Cases & Error Handling

| Skenario | Perilaku yang diharapkan |
|---|---|
| API down / tidak bisa diakses | Pesan error jelas, fallback ke Library/cache lokal |
| Kena rate limit (response error/429) | Tampilkan pesan "coba lagi sebentar", auto-retry dengan delay — bukan retry beruntun |
| Manga hilang dari sumber API | Tetap muncul di Library dengan tag "sumber tidak tersedia", history tetap tersimpan |
| Chapter gagal load gambar | Retry otomatis + tombol retry manual per halaman |
| User bookmark manga yang sama dua kali | Update status yang ada, bukan bikin entry duplikat |
| Koneksi lemah saat baca | Progressive image loading + indikator loading per halaman |

---

## 12. Risiko & Mitigasi

| Risiko | Mitigasi |
|---|---|
| Rate limit 30/menit gampang kelampaui kalau request tidak diatur | Debounce search, cache agresif, hindari batch-fetch thumbnail sekaligus |
| 3x pelanggaran rate limit → ban permanen per IP | Request queue + retry-backoff di client sejak desain awal, bukan tambahan belakangan |
| Tidak ada versioning API, breaking change tanpa notice | Isolasi parsing logic di `core-network`, pantau WhatsApp Channel/GitHub Sanka untuk update |
| Field status publikasi mungkin tidak eksplisit di respons detail | Validasi langsung di Fase 0; siapkan fallback derive dari teks description kalau field terstruktur tidak ada |
| Konten scraping berpotensi masalah hak cipta | Pertimbangkan matang jalur distribusi (Play Store vs sideload/APK langsung) |

---

## 13. Diferensiasi

| Aspek | App ini | Reader manga umum (mis. Tachiyomi/Mihon-style) |
|---|---|---|
| Auto-tag status publikasi | Ya, otomatis dari API | Manual, tergantung extension yang dipakai |
| Sumber konten | 1 API terpusat, gratis tanpa key | Multi-extension, banyak sumber |
| Setup awal | Langsung pakai | Perlu install extension terpisah |

---

## 14. Keputusan Terbuka

1. **Field status publikasi** — sample dokumentasi cuma nunjukin field hasil search (title, slug, thumbnail, type, genre, description), belum ada contoh respons detail (`/comic/comic/:slug`) secara lengkap. Perlu dicek langsung apakah field status (tamat/ongoing) tersedia eksplisit
2. **Konfirmasi source default** — endpoint comic default kemungkinan besar = Komiku (berdasarkan URL di sample respons), tapi belum dinyatakan eksplisit di dokumentasi seperti halnya anime ("Otakudesu — Default")
3. **Include source 18+ (Mangasusuku)?** — default: tidak, di-exclude dari MVP. Kalau nanti mau diaktifkan, wajib age-gate
4. **Strategi distribusi** — Play Store (risiko terkait konten scraping) vs sideload/APK langsung
5. **Nama & positioning app** — belum ditentukan

---

## 15. Fase Pengembangan

- **Fase 0 — Validasi teknis:** test langsung `GET /comic/comic/:slug` buat pastiin field status publikasi tersedia; konfirmasi source default; bangun `core-network` dengan caching & rate-limit guard sejak awal
- **Fase 1 — MVP:** implementasi FR-1 s/d FR-10
- **Fase 2 — Enhancement:** notifikasi chapter baru, download offline, multi-source (Shinigami/Komikstation sebagai opsi tambahan)

---

*Referensi API: dokumentasi live `/comic`, `/docs`, dan README GitHub `SankaVollereii/Rest-Api-Anime-and-Comic`, diverifikasi 27 Agustus 2026.*
