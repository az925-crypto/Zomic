package com.zaaamzomic.data.network

import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLDecoder

/**
 * Lightweight interceptor that only blocks 18+ sources, no rate limiting.
 * Used for image OkHttp client (Coil) so images don't bypass the API guard.
 */
class Block18Interceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val urlString = chain.request().url.toString().lowercase()
        val decoded = try { URLDecoder.decode(urlString, "UTF-8") } catch (_: Exception) { urlString }
        // double-decode to catch %256D etc.
        val doubleDecoded = try { URLDecoder.decode(decoded, "UTF-8") } catch (_: Exception) { decoded }
        if (doubleDecoded.contains("mangasusuku") || doubleDecoded.contains("nekopoi") || decoded.contains("mangasusuku") || decoded.contains("nekopoi")) {
            throw IllegalArgumentException("Source 18+ blocked: ${chain.request().url}")
        }
        return chain.proceed(chain.request())
    }
}
