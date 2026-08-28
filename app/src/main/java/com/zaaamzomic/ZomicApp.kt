package com.zaaamzomic

import android.app.Application
import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.zaaamzomic.data.db.ZomicDatabase
import com.zaaamzomic.data.network.RateLimitInterceptor
import com.zaaamzomic.data.network.SankaComicService
import com.zaaamzomic.data.network.createOkHttpClient
import com.zaaamzomic.data.network.createOkHttpClientForImages
import com.zaaamzomic.data.network.createRetrofit
import kotlinx.serialization.json.Json
import okhttp3.Cache
import java.io.File

class ZomicApp : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(): ImageLoader = container.imageLoader
}

class AppContainer(val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val okHttpCache by lazy {
        Cache(File(context.cacheDir, "http_cache"), 10L * 1024 * 1024)
    }

    private val imageCache by lazy {
        Cache(File(context.cacheDir, "http_image_cache"), 10L * 1024 * 1024)
    }

    private val rateLimitInterceptor by lazy { RateLimitInterceptor() }

    val okHttpClient by lazy {
        createOkHttpClient(okHttpCache, rateLimitInterceptor, isDebug = com.zaaamzomic.BuildConfig.DEBUG)
    }

    // Separate client for Coil — no rate limit, to avoid starving image loads
    val imageOkHttpClient by lazy {
        createOkHttpClientForImages(imageCache, isDebug = com.zaaamzomic.BuildConfig.DEBUG)
    }

    val sankaService: SankaComicService by lazy {
        createRetrofit(okHttpClient, json).create(SankaComicService::class.java)
    }

    val db: ZomicDatabase by lazy {
        Room.databaseBuilder(context, ZomicDatabase::class.java, "zomic.db")
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    val libraryRepository by lazy {
        com.zaaamzomic.data.db.LibraryRepository(db.mangaDao())
    }

    val imageLoader by lazy {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "coil_cache"))
                    .maxSizePercent(0.02)
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .okHttpClient(imageOkHttpClient)
            .crossfade(true)
            .build()
    }
}
