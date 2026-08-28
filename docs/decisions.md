# Keputusan Desain Zomic

| ID | Topik | Keputusan | Alasan |
|---|---|---|---|
| A1 | Package | com.zaaamzomic (3 a) sesuai note.txt literal | Jika perlu uniform 4 a (kalku), refactor 1 commit |
| A2 | Network | Retrofit 2.9.0 + OkHttp 4.12.0 + dual OkHttpClient (API rate-limited vs image non-limited) | Stabil, image tidak starve token bucket |
| A3 | DB | Room 2.6.1 single DB v1, tabel manga_library + enum converters, exportSchema true, Mutex untuk bookmark/sync/markUnavailable/saveProgress | Hindari lost-update race; migration-guard strict, schema placeholder di-update CI |
| A4 | DI | Hand-rolled container (ZomicApp.container) tanpa Hilt untuk MVP | Kurangi build time CI |
| A5 | Reading mode | Vertical scroll LazyColumn + snapshotFlow + debounced saveProgress 500ms | Jank-free, no ANR |
| A6 | Compose BOM | 2024.12.01 stable | Hindari beta break |
| B1 | Nama app | Zomic (dari repo) | Bisa ganti ke Zomic Reader jika user approve |
| B2 | Distribusi | Sideload via GitHub Release untuk MVP, CI commit schema auto | Scraping-based risiko Play Store |
| B3 | 18+ | Exclude Mangasusuku/Nekopoi (Block18Interceptor double-decode, https-only) | Sesuai PRD |
| B4 | Fallback status | Heuristic \btamat\b/\bcompleted\b + belum tamat first, UNKNOWN jujur | Hindari false positive ketamatan |
| B5 | Rate limit | Token bucket SystemClock.elapsedRealtime 30/60s, single sleep per 429, maxRequestsPerHost 15 | Anti BAN permanen Sanka |
| B6 | Reader retry | key(retryKey) + hasError manual, no auto onError loop | Anti infinite retry OOM |
