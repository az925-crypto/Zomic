package com.zaaamzomic.data.network

import android.os.SystemClock
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLDecoder

/**
 * Token bucket: 30 requests per 60 seconds.
 * Queues requests when bucket empty, retries 429 with backoff.
 * Blocks Mangasusuku / Nekopoi (18+ excluded from MVP).
 */
class RateLimitInterceptor(
    private val maxRequests: Int = 30,
    private val windowSeconds: Long = 60L,
) : Interceptor {

    private val requestTimes = mutableListOf<Long>()
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val urlString = request.url.toString().lowercase()
        fun doubleDecode(s: String): String {
            val once = try { URLDecoder.decode(s, "UTF-8").lowercase() } catch (_: Exception) { s.lowercase() }
            return try { URLDecoder.decode(once, "UTF-8").lowercase() } catch (_: Exception) { once }
        }
        val decodedPath = doubleDecode(request.url.encodedPath)
        val decodedUrl = doubleDecode(urlString)

        // Block 18+ sources per PRD §10 — check full URL decoded + path + query (double decode for %256D bypass)
        if (decodedPath.contains("mangasusuku") || decodedPath.contains("nekopoi") ||
            decodedUrl.contains("mangasusuku") || decodedUrl.contains("nekopoi")
        ) {
            throw IllegalArgumentException("Source 18+ blocked in MVP: ${request.url}")
        }

        // Acquire token (blocking with delay if needed)
        acquireTokenBlocking()

        var attempt = 0
        while (attempt < 3) {
            val response = chain.proceed(request)
            if (response.code != 429) return response
            val retryAfter = response.header("Retry-After")?.toLongOrNull()?.let { it * 1000 } ?: when (attempt) {
                0 -> 1000L
                1 -> 4000L
                else -> 8000L
            }
            response.close()
            if (attempt == 2) break
            Thread.sleep(retryAfter)
            attempt++
        }
        // All retries exhausted — final attempt
        return chain.proceed(request)
    }

    private fun acquireTokenBlocking() {
        while (true) {
            val waitMs = synchronized(lock) {
                val now = SystemClock.elapsedRealtime()
                requestTimes.removeAll { now - it > windowSeconds * 1000 }
                if (requestTimes.size < maxRequests) {
                    requestTimes.add(now)
                    -1L
                } else {
                    val oldest = requestTimes.minOrNull() ?: now
                    val nextAvailable = oldest + windowSeconds * 1000
                    nextAvailable - now
                }
            }
            if (waitMs < 0) return
            val sleep = waitMs.coerceAtMost(5000L).coerceAtLeast(0L)
            Thread.sleep(sleep)
        }
    }
}
