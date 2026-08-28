# 📚 Catatan API Sanka Vollerei — `sankavollerei.web.id`

> **Ringkasan:** REST API **GRATIS** Anime, Komik/Manga, Donghua, Novel & Tools Developer Indonesia. Aktif sejak **22 Mei 2025**. Cocok untuk Bot WhatsApp, Web App, Mobile App, dll.
> 
> **Base URL:** `https://www.sankavollerei.web.id`
> 
> **Repo GitHub:** `https://github.com/SankaVollereii/Rest-Api-Anime-and-Comic`
> 
> **Update catatan:** 27 Agustus 2026 — diverifikasi langsung via `fetch` ke endpoint live

---

## 📑 Daftar Isi

- [Informasi Umum](#-informasi-umum)
- [Autentikasi & Rate Limit](#-autentikasi--rate-limit)
- [Struktur Respons Umum](#-struktur-respons-umum)
- [API Anime](#-api-anime-120-endpoint)
- [API Komik / Manga](#-api-komik--manga-100-endpoint)
- [API Novel](#-api-novel)
- [Tools & Layanan Lain](#-tools--layanan-lain)
- [Contoh Penggunaan (JS / Python / PHP / cURL)](#-contoh-penggunaan)
- [Kontak & Support](#-kontak--support)
- [Catatan Penting & Disclaimer](#-catatan-penting--disclaimer)

---

## 🌐 Informasi Umum

| Item | Detail |
|------|--------|
| **Nama** | Sanka Vollerei API / API Sanka / Plana API |
| **Base URL** | `https://www.sankavollerei.web.id` |
| **Docs UI Anime** | `GET https://www.sankavollerei.web.id/anime` (HTML interaktif) |
| **Docs UI Comic** | `GET https://www.sankavollerei.web.id/comic` |
| **Docs Umum** | `GET https://www.sankavollerei.web.id/docs` |
| **Total Endpoint** | 120+ Anime • 100+ Comic • Novel |
| **Database** | 50.000+ judul Anime • 100.000+ seri Manga/Manhwa/Manhua |
| **Klaim Performansi** | <100ms avg response • 99.9% uptime • 10jt+ request/bulan • Realtime update |
| **Multi-bahasa** | EN, ID, JP |
| **Biaya** | Gratis untuk personal & komersial |
| **Lisensi** | MIT (repo GitHub) |

Sumber scraping mencakup Otakudesu, Samehadaku, Donghua, Kusonime, Anoboy, Oploverz, Anime Indo, Animekuindo, Nimegami, Alqanime, Donghub, Winbu, Animekompi, Kuramanime, Nekopoi, Komiku, BacaKomik, Komikstation, Maid, Komikindo, Mangakita, SoulScans, Meganei, Komikcast, Bacaman, Softkomik, Westmanga, Kiryuu, Shinigami, Cosmic Scans, Mangasusuku, Novelhub, SakuraNovel, dll.

---

## 🔐 Autentikasi & Rate Limit

| Item | Nilai |
|------|-------|
| **API Key** | **Tidak wajib** untuk akses dasar. Halaman `/docs` menyebut FREE APIKEY: `planaai` (opsional) |
| **Rate Limit (halaman web)** | **30 request / menit / IP** — tercantum di `/anime` & `/comic` dengan peringatan tegas |
| **Rate Limit (README GitHub)** | 1000 request / jam / IP — anggap sebagai batas alternatif |
| **Batas Aman** | Patuhi **30/menit**. Jangan burst/spam. Cache response bila memungkinkan |
| **Sanksi** | 3x peringatan → **BAN PERMANEN**. Unban GRATIS via kontak owner |
| **Whitelist** | Hubungi owner jika butuh limit lebih tinggi |

> ⚠️ Gunakan nilai **30/menit** sebagai patokan. Jika butuh lebih, minta whitelist ke Telegram `@OnlySankaaa`.

---

## 📦 Struktur Respons Umum

Semua endpoint mengembalikan JSON dengan wrapper konsisten:

```json
{
  "status": "success",
  "creator": "Sanka Vollerei",
  "statusCode": 200,
  "statusMessage": "OK",
  "message": "",
  "ok": true,
  "data": { ... },
  "pagination": null
}
```

Variasi pada endpoint comic (contoh `GET /comic/search?q=naruto`):

```json
{
  "status": true,
  "creator": "Sanka Vollerei",
  "message": "Berhasil mendapatkan hasil pencarian",
  "q": "naruto",
  "url": "https://komiku.org/?s=naruto&post_type=manga",
  "total": 4,
  "data": [
    {
      "title": "Naruto",
      "slug": "naruto",
      "href": "/detail-komik/naruto/",
      "thumbnail": "https://thumbnail.komiku.org/...",
      "type": "Manga",
      "genre": "Aksi",
      "description": "Update 1 tahun lalu."
    }
  ]
}
```

Contoh `GET /anime/home` → `data.ongoing.animeList[]` & `data.completed.animeList[]`:

```json
{
  "status": "success",
  "data": {
    "ongoing": {
      "href": "/anime/ongoing-anime",
      "animeList": [
        {
          "title": "Re:Zero kara Hajimeru Isekai Seikatsu Season 4",
          "poster": "https://otakudesu.blog/...",
          "episodes": 14,
          "releaseDay": "Rabu",
          "latestReleaseDate": "26 Agu",
          "animeId": "re-zero-kara-s4-sub-indo",
          "href": "/anime/anime/re-zero-kara-s4-sub-indo",
          "otakudesuUrl": "https://otakudesu.blog/anime/..."
        }
      ]
    },
    "completed": { "href": "/anime/complete-anime", "animeList": [...] }
  }
}
```

### Pola URL Umum

```
Anime : /anime/:source/endpoint
Comic : /comic/:source/endpoint
Novel : /novel/:source/endpoint
```

- Semua method adalah **GET**
- Pagination umumnya via `?page=1` atau `/:page` (path param)
- Filter via query string: `?q=`, `?genre=`, `?status=`, `?order=`, `?type=`, dll (berbeda tiap source)

---

## 🎬 API Anime (120+ Endpoint)

> **Cara akses docs interaktif:** buka `https://www.sankavollerei.web.id/anime` di browser — semua endpoint bisa di-klik & di-try.

### 1. Otakudesu — Default (tanpa prefix source)

| Endpoint | Deskripsi |
|----------|-----------|
| `GET /anime` | UI HTML dokumentasi lengkap |
| `GET /anime/home` | Rilis terbaru (default Otakudesu) |
| `GET /anime/schedule` | Jadwal rilis per hari |
| `GET /anime/anime/:slug` | Detail lengkap anime + daftar episode. Cth: `/anime/anime/enen-shouboutai-season-3-p2-sub-indo` |
| `GET /anime/complete-anime?page=1` | Anime tamat per halaman |
| `GET /anime/ongoing-anime?page=1` | Anime ongoing |
| `GET /anime/genre` | Daftar semua genre |
| `GET /anime/genre/:slug?page=1` | Anime by genre. Cth: `/anime/genre/action?page=1` |
| `GET /anime/episode/:slug` | Detail + link stream/download episode. Cth: `/anime/episode/mebsn-episode-1-sub-indo` |
| `GET /anime/search/:keyword` | Cari anime. Cth: `/anime/search/boruto` |
| `GET /anime/batch/:slug` | Link download batch. Cth: `/anime/batch/jshk-s2-batch-sub-indo` |
| `GET /anime/server/:serverId` | URL embed streaming. Cth: `/anime/server/6DC77B-6-8B5u` |
| `GET /anime/unlimited` | Semua data anime |

### 2. Donghua — `/anime/donghua/`

| Endpoint | Deskripsi |
|----------|-----------|
| `GET /anime/donghua/home/:page?` | Home Donghua |
| `GET /anime/donghua/ongoing/:page?` | Donghua ongoing |
| `GET /anime/donghua/completed/:page?` | Donghua completed |
| `GET /anime/donghua/latest/:page?` | Donghua latest update |
| `GET /anime/donghua/schedule` | Jadwal tayang |
| `GET /anime/donghua/az-list/:slug/:page?/` | By huruf |
| `GET /anime/donghua/search/:keyword/:page?` | Pencarian |
| `GET /anime/donghua/detail/:slug` | Detail donghua |
| `GET /anime/donghua/episode/:slug` | Stream & download episode |
| `GET /anime/donghua/genres` | Semua genre |
| `GET /anime/donghua/genres/:slug/:page?` | By genre |
| `GET /anime/donghua/seasons/:year?` | By tahun rilis |

### 3. Samehadaku — `/anime/samehadaku/`

| Endpoint | Deskripsi |
|----------|-----------|
| `GET /anime/samehadaku/home` | Home Samehadaku |
| `GET /anime/samehadaku/recent?page=2` | Anime terbaru |
| `GET /anime/samehadaku/search?q=one%20piece&page=1` | Pencarian (`q` wajib) |
| `GET /anime/samehadaku/ongoing?page=1&order=popular` | Ongoing |
| `GET /anime/samehadaku/completed?page=1&order=latest` | Completed |
| `GET /anime/samehadaku/popular?page=1` | Terpopuler |
| `GET /anime/samehadaku/movies?page=1&order=update` | Movie |
| `GET /anime/samehadaku/list` | Semua anime |
| `GET /anime/samehadaku/schedule` | Jadwal mingguan |
| `GET /anime/samehadaku/genres` | Semua genre |
| `GET /anime/samehadaku/genres/:genreId?page=1` | By genre |
| `GET /anime/samehadaku/batch?page=1` | Daftar batch |
| `GET /anime/samehadaku/anime/:animeId` | Detail anime |
| `GET /anime/samehadaku/episode/:episodeId` | Detail episode |
| `GET /anime/samehadaku/batch/:batchId` | Detail batch |
| `GET /anime/samehadaku/server/:serverId` | Link embed server |

### 4. Animasu — `/anime/animasu/`

`home?page=1`, `popular?page=1`, `movies?page=1`, `ongoing?page=1`, `completed?page=1`, `latest?page=1`, `search/:keyword?page=1`, `animelist?letter=A&page=1`, `advanced-search?genres=aksi&status=ongoing&page=1`, `genres`, `genre/:slug?page=1`, `characters`, `character/:slug?page=1`, `schedule`, `detail/:slug`, `episode/:slug`

### 5. Kusonime — `/anime/kusonime/`

`latest?page=1`, `all-anime?page=1`, `movie?page=1`, `type/:type` (ova/ona/special), `all-genres`, `all-seasons`, `search/:query?page=1`, `genre/:slug?page=1`, `season/:season/:year`, `detail/:slug`

### 6. Anoboy — `/anime/anoboy/`

`home?page=1`, `search/:keyword?page=1`, `anime/:slug`, `episode/:slug`, `az-list?page=1&show=A`, `list?status=ongoing&type=tv&order=update`, `genre/:slug?page=1`, `genres`

### 7. Oploverz — `/anime/oploverz/`

`home?page=1`, `schedule`, `ongoing?page=1`, `completed?page=1`, `list?status=&type=&order=`, `search/:query`, `anime/:slug`, `episode/:slug`

### 8. Stream / Anime Indo — `/anime/stream/`

`latest/:page`, `popular`, `search/:query`, `anime/:slug`, `episode/:slug`, `movie/:page`, `list`, `genres`, `genres/:slug/:page`

### 9. Animekuindo — `/anime/animekuindo/`

`home?page=1`, `schedule`, `latest?page=1`, `popular?page=1`, `movie?page=1`, `search/:query`, `genres`, `genres/:slug`, `seasons`, `seasons/:slug`, `detail/:slug`, `episode/:slug`

### 10. Nimegami — `/anime/nimegami/` (semua status Working)

`home?page=1`, `search/:query?page=1`, `detail/:slug`, `anime-list?page=1`, `genre/list`, `genre/:slug`, `seasons/list`, `seasons/:slug`, `type/list`, `type/:slug`, `j-drama`, `live-action`, `live-action/:slug`, `drama/:slug`

### 11. Alqanime — `/anime/alqanime/`

`home?page=1`, `schedule`, `popular?page=1`, `list?show=A`, `ongoing?page=1`, `completed?page=1`, `movie?page=1`, `search/:query?page=1`, `genres`, `genre/:slug?page=1`, `season/:slug`, `detail/:slug`

### 12. Donghub — `/anime/donghub/`

`home?page=1`, `latest?page=1`, `popular?page=1`, `movie?page=1`, `schedule`, `search/:query`, `genre/:slug`, `list?sub=&order=`, `detail/:slug`, `episode/:slug`

### 13. Winbu — `/anime/winbu/`

`home`, `search?q=&page=1`, `anime/:id`, `series/:id`, `film/:id`, `episode/:id`, `server?post=&nume=&type=schtml`, `animedonghua?page=1`, `film?page=1`, `series?page=1`, `tvshow?page=1`, `others?page=1`, `genres`, `genre/:slug`, `catalog?title=&page=&order=&type=&status=`, `schedule?day=senin`, `update`, `latest`, `ongoing`, `completed`, `populer`, `all-anime?page=1`, `all-anime-reverse?page=1`, `list?order=&status=&type=`

### 14. Animekompi — `/anime/animekompi/`

`home?page=1`, `terbaru?page=1`, `donghua?page=1`, `live-action?page=1`, `tokusatsu?page=1`, `movie?page=1`, `schedule`, `list`, `search?q=&page=1`, `search/suggest?q=`, `filter?genre[]=&studio[]=&order=`, `filterlist`, `genres`, `seasons`, `studios`, `status`, `types`, `orders`, `genre/:slug`, `season/:slug`, `studio/:slug`, `status/:slug`, `type/:slug`, `order/:slug`, `detail/:slug`, `episode/:slug`, `tooltip/:id`

### 15. Kuramanime — `/anime/kura/` (label `kura`, ada warning error di docs)

`home`, `search/:keyword`, `anime/:id/:slug`, `watch/:id/:slug/:episode` (butuh 15-30dtk), `batch/:id/:slug/:batchId`, `anime-list?page=&order_by=`, `schedule?scheduled_day=`, `quick/popular?page=`, `quick/ongoing?page=&order_by=`, `quick/finished?page=&order_by=`, `quick/movie?page=&order_by=`, `quick/donghua?page=&order_by=`, `properties/genre`, `properties/genre/:slug`, `properties/season`, `properties/season/:slug`, `properties/studio`, `properties/studio/:slug`, `properties/type`, `properties/type/:slug`, `properties/quality`, `properties/quality/:slug`, `properties/source`, `properties/source/:slug`, `properties/country`, `properties/country/:slug`

### 16. Nekopoi — `/anime/nekopoi/` (🔞 18+)

`home`, `search?q=&page=1`, `hentai-list?page=1`, `genres`, `genre/:slug`, `category/:slug` (cth: `3d-hentai`, `jav-cosplay`), `latest-hentai?page=1`, `latest-jav?page=1`, `detail/:slug`, `episode/:slug`

---

## 📖 API Komik / Manga (100+ Endpoint)

> Docs interaktif: `https://www.sankavollerei.web.id/comic`

### 1. Default / Multi-source — `/comic/`

| Endpoint | Deskripsi |
|----------|-----------|
| `GET /comic` | UI dokumentasi |
| `GET /comic/unlimited` | Akses 6.297+ komik (deep crawl) |
| `GET /comic/scroll` | Infinite scroll simulation |
| `GET /comic/docs` | Statistik + dokumentasi |
| `GET /comic/realtime` | Data real-time (parallel fetch) |
| `GET /comic/comparison` | Perbandingan performa |
| `GET /comic/terbaru` | Komik terbaru |
| `GET /comic/populer` | Komik populer (multi-source) |
| `GET /comic/search?q=naruto` | Pencarian (3-method fallback) |
| `GET /comic/comic/:slug` | Detail komik + daftar chapter |
| `GET /comic/chapter/:slug` | Gambar chapter |
| `GET /comic/trending` | Trending (multi timeframe) |
| `GET /comic/fullstats` | Statistik lengkap |
| `GET /comic/type/:type` | Filter by type (manga/manhwa/manhua) |
| `GET /comic/homepage` | Data homepage (popular/latest/ranking) |
| `GET /comic/chapter/:slug/navigation` | Navigasi prev/next chapter |
| `GET /comic/genres` | List semua genre |
| `GET /comic/random` | Komik random |
| `GET /comic/stats` | Statistik umum |
| `GET /comic/infinite` | Infinite load |
| `GET /comic/browse` | Browse dengan filter type/order/genre |
| `GET /comic/genre/:genre` | By genre |
| `GET /comic/advanced-search` | Advanced search multi-filter |
| `GET /comic/favorites` | Bookmark (butuh auth) |
| `GET /comic/recommendations` | Rekomendasi |
| `GET /comic/analytics` | Analytics detail |
| `GET /comic/berwarna/:page` | Komik berwarna |
| `GET /comic/pustaka/:page` | Pustaka |
| `GET /comic/health` | Health check |

### 2. BacaKomik — `/comic/bacakomik/`

`latest`, `populer`, `only/:type` (manga/manhwa/manhua), `top`, `list`, `search/:query`, `genres`, `genre/:genre`, `detail/:slug`, `chapter/:slug`, `recomen`, `komikberwarna/:page`

### 3. Maid Comic — `/comic/maid/`

`list`, `api` (hot projects), `latest?page=1`, `manga/:slug`, `chapter/:slug`, `genres`, `genres/:slug?page=`, `search?title=&page=`

### 4. Komikindo — `/comic/komikindo/`

`latest/:page`, `detail/:slug`, `chapter/:slug`, `library?page=1`, `genres`, `search/:query/:page`, `config`, `list?genre=&status=`, `populer/:page`, `type/:type/:page`, `colorized/:val/:page`, `detail/:id`, `chapter/:id`, `filter/:term/:val/:page`

> Catatan: ada dua prefix terkait — `/comic/komikindo/` dan `/comic/kmkindo/` (alias). Struktur mirip.

### 5. Mangakita — `/comic/mangakita/`

`home`, `list?order=&page=`, `projects/:page`, `daftar-manga/:page`, `genres`, `genres/:slug/:page`, `rekomendasi`, `search/:query/:page`, `detail/:slug`, `chapter/:slug`

### 6. Meganei — `/comic/meganei/` (batch, PW: `meganei.net`)

`home/:page`, `list?page=`, `search/:query`, `info/:slug`

### 7. Softkomik — `/comic/softkomik/`

`home`, `list?page=1` (max 287 pages), `update`, `ongoing?page=1`, `completed?page=1`, `library?page=1&sort=newKomik`, `type/:type?page=1`, `search?q=`, `genres`, `genre/:name`, `detail/:slug`, `chapter/:slug/:ch`

### 8. Westmanga — `/comic/westmanga/`

`home`, `genres`, `list?page=1`, `latest?page=1`, `popular?page=1`, `ongoing?page=1`, `completed?page=1`, `manga?page=1` (JP), `manhua?page=1` (CN), `manhwa?page=1` (KR), `az?page=1`, `za?page=1`, `added?page=1`, `colored?page=1`, `uncolored?page=1`, `projects?page=1`, `others?page=1`, `genre/:id?page=1`, `genres-filter?ids=13,344&page=1`, `search?q=&page=1`, `detail/:slug`, `chapter/:slug`

### 9. Kiryuu — `/comic/kiryuu/`

`home`, `popular`, `recommendations`, `latest`, `top-weekly`, `search/:query/:page`, `manga/:slug`, `chapter/:slug`

### 10. Shinigami — `/comic/shinigami/`

`home`, `slider/:category`, `explore/:category`, `latest?page=&page_size=`, `popular?page=&page_size=`, `recommended?page=`, `search/:query`, `detail/:manga_id` (UUID), `chapters/:manga_id`, `read/:chapter_id` (UUID), `list?format=&page=`, `genres`, `formats`, `types`, `authors?q=&page=`, `artists?q=&page=`, `advanced-search?genre_include=&sort=`

### 11. Komikstation — `/comic/komikstation/` (docs label error)

`list?type=&status=&order=&page=`, `home`, `popular?page=`, `recommendation`, `top-weekly`, `ongoing?page=`, `az-list/:letter?page=`, `genres`, `genre/:slug/:page`, `search/:query/:page`, `manga/:slug`, `chapter/:slug`

### 12. Mangasusuku — `/comic/mangasusuku/` (🔞 18+)

`home/:page`, `latest/:page`, `popular/:page`, `list/:page`, `list-by-char/:char/:page`, `search/:query/:page`, `genres`, `genre/:genreId/:page`, `detail/:slug`, `chapter/:slug`

> Sumber lain tercantum di repo namun belum ada endpoint stabil: **SoulScans, Komikuindo, Komikcast, Bacaman, Cosmic Scans, MeioNovel** — cek `/comic` untuk update terbaru.

---

## 📚 API Novel

### Default — `/novel/`

| Endpoint | Deskripsi |
|----------|-----------|
| `GET /novel/home` | Curated lists & genres |
| `GET /novel/hot-search` | Novel trending di pencarian |
| `GET /novel/search?q=keyword` | Cari novel |
| `GET /novel/genre/:id` | Browse by genre ID |
| `GET /novel/chapters/:novelId` | Daftar chapter novel |

### SakuraNovel — `/novel/sakuranovel/`

`home?page=1`, `search?q=`, `advanced-search?status=`, `detail/:slug`, `read/:slug`, `genres`, `genre/:slug`, `tags`, `tag/:slug`, `daftar-novel`

---

## 🛠️ Tools & Layanan Lain

| Layanan | URL | Deskripsi |
|---------|-----|-----------|
| **Anime Downloader** | `https://www.sankavollerei.web.id/download/anime` | UI download anime (Otakudesu/Samehadaku/Donghua) + demo API. Support 360p/480p/720p/1080p |
| **Uploader / Tools Sanka** | `https://www.sankavollerei.web.id/tools` | Kumpulan tools internal |
| **Plana Store** | `https://www.sankavollerei.web.id/store` | Toko produk digital |
| **Plana AI Chat** | Widget di homepage | Chat AI di situs utama |
| **Sankanime Streaming** | `https://link.sankanime.web.id` / `https://sankanime.web.id` | Platform streaming anime tanpa iklan, HD, subtitle ID |
| **Docs API** | `https://www.sankavollerei.web.id/docs` | Dokumentasi umum (FREE APIKEY: `planaai`) |

---

## 💻 Contoh Penggunaan

### cURL

```bash
# Anime — cari Boruto (Otakudesu default)
curl https://www.sankavollerei.web.id/anime/search/boruto

# Anime — detail & episode
curl https://www.sankavollerei.web.id/anime/anime/enen-shouboutai-season-3-p2-sub-indo
curl https://www.sankavollerei.web.id/anime/episode/mebsn-episode-1-sub-indo

# Anime — Samehadaku search
curl "https://www.sankavollerei.web.id/anime/samehadaku/search?q=one%20piece"

# Anime — Kuramanime detail (butuh ID + slug)
curl https://www.sankavollerei.web.id/anime/kura/anime/3138/dandadan

# Comic — search
curl "https://www.sankavollerei.web.id/comic/search?q=naruto"

# Comic — detail & chapter
curl https://www.sankavollerei.web.id/comic/comic/naruto
curl https://www.sankavollerei.web.id/comic/chapter/naruto-chapter-1

# Comic — Kiryuu
curl https://www.sankavollerei.web.id/comic/kiryuu/search/one%20piece

# Novel
curl "https://www.sankavollerei.web.id/novel/search?q=The%20Millennium%20Wolves"
```

### JavaScript / Node.js

```javascript
// Anime — search
async function searchAnime(query) {
  const res = await fetch(`https://www.sankavollerei.web.id/anime/search/${encodeURIComponent(query)}`);
  const json = await res.json();
  console.log(json.data.animeList);
}

// Comic — search
async function searchComic(query) {
  const res = await fetch(`https://www.sankavollerei.web.id/comic/search?q=${encodeURIComponent(query)}`);
  const json = await res.json();
  console.log(json.data);
}

// Samehadaku — search dengan query string
async function searchSamehadaku(q) {
  const res = await fetch(`https://www.sankavollerei.web.id/anime/samehadaku/search?q=${encodeURIComponent(q)}`);
  return res.json();
}

// Detail anime
async function getAnimeDetail(slug) {
  const res = await fetch(`https://www.sankavollerei.web.id/anime/anime/${slug}`);
  return res.json();
}

searchAnime("boruto");
```

### Python

```python
import requests

BASE = "https://www.sankavollerei.web.id"

def search_anime(query):
    r = requests.get(f"{BASE}/anime/search/{query}")
    r.raise_for_status()
    return r.json()

def search_comic(query):
    r = requests.get(f"{BASE}/comic/search", params={"q": query})
    r.raise_for_status()
    return r.json()

def get_episode(slug):
    r = requests.get(f"{BASE}/anime/episode/{slug}")
    r.raise_for_status()
    return r.json()

print(search_anime("boruto"))
print(search_comic("naruto"))
```

### PHP

```php
<?php
function sanka_get($path) {
    $url = "https://www.sankavollerei.web.id" . $path;
    $json = file_get_contents($url);
    return json_decode($json, true);
}

// Anime by genre
$data = sanka_get("/anime/genre/action?page=1");
print_r($data);

// Comic latest
$data = sanka_get("/comic/terbaru");
print_r($data);

// Novel search
$data = sanka_get("/novel/search?q=love");
print_r($data);
?>
```

---

## 📞 Kontak & Support

| Channel | Link / Handle |
|---------|---------------|
| **Website** | https://www.sankavollerei.web.id |
| **GitHub** | https://github.com/SankaVollereii |
| **Repo API** | https://github.com/SankaVollereii/Rest-Api-Anime-and-Comic |
| **Telegram** | [@OnlySankaaa](https://t.me/OnlySankaaa) |
| **WhatsApp Channel** | https://www.whatsapp.com/channel/0029Vb7fWF61CYoQMRaXBM1X |
| **Contact Page** | https://www.sankavollerei.web.id/about |
| **Discord** | https://discord.gg/QrYdsSThM4 |
| **Instagram** | https://instagram.com/sandikaaa_78 |
| **Portfolio** | https://portfolio.sankavollerei.web.id |
| **Donasi / Support** | https://trakteer.id/Sankanime/gift • https://www.sankavollerei.web.id/donate |
| **Email (docs)** | support@sankavollerei.com |

---

## ⚠️ Catatan Penting & Disclaimer

1. **Gratis & tanpa API key wajib**, tapi hormati **rate limit 30/menit**. Cache respons agar tidak membebani server.
2. **Masih dalam pengembangan** — beberapa endpoint mungkin belum stabil atau mengembalikan error (terutama Kuramanime, sebagian Komikstation, dan beberapa filter). Cek halaman `/anime` & `/comic` untuk status terbaru.
3. **Konten 18+** tersedia di `/anime/nekopoi/*` dan `/comic/mangasusuku/*` — gunakan filter umur di aplikasi Anda.
4. **Scraping-based** — metadata diambil dari situs publik (Otakudesu dkk). Hormati hak cipta & ToS sumber asli. Sanka menyatakan akan menangani laporan hak cipta dengan cepat.
5. **Embed/stream link** bisa berupa URL pihak ketiga atau memerlukan `serverId` untuk resolve. Untuk Kuramanime `/kura/watch/...` butuh 15–30 detik.
6. **jangan abuse** — 3x peringatan → BAN permanen per IP. Jika terkena ban, hubungi owner untuk unban gratis.
7. **Versioning** — tidak ada header versi eksplisit; perubahan bisa terjadi tanpa pemberitahuan. Pantau WhatsApp Channel & GitHub untuk update.

---

## 📊 Statistik Klaim (dari repo & site)

> 120+ Anime Endpoints • 22.297+ Anime (panel Try-It) / 50.000+ (README) • 100+ Comic Endpoints • 20.297+ Comics • 315x Performance Improvement • <100ms avg • 99.9% uptime • 10M+ calls/bulan

*Angka bisa berbeda antara halaman docs dan README — gunakan angka terbaru dari halaman live.*

---

*Disusun dari dokumentasi live di `/anime`, `/comic`, `/download/anime`, `/docs`, dan README GitHub `SankaVollereii/Rest-Api-Anime-and-Comic`. Diverifikasi dengan request nyata ke `/anime/home`, `/anime/search/boruto`, dan `/comic/search?q=naruto` pada 27 Aug 2026.*

*Butuh integrasi cepat? Buka `/anime` atau `/comic` di browser, klik endpoint, copy URL — langsung pakai.*
