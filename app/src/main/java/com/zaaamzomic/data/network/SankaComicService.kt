package com.zaaamzomic.data.network

import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

interface SankaComicService {
    @GET("/comic/terbaru")
    suspend fun getTerbaru(): TerbaruWrapper

    @GET("/comic/populer")
    suspend fun getPopuler(): TerbaruWrapper

    @GET("/comic/trending")
    suspend fun getTrending(): TrendingWrapper

    @GET("/comic/genres")
    suspend fun getGenres(): GenresWrapper

    @GET("/comic/genre/{genre}")
    suspend fun getGenreComics(@Path("genre") genre: String): GenreComicsWrapper

    @GET("/comic/berwarna/{page}")
    suspend fun getBerwarna(@Path("page") page: Int): BerwarnaWrapper

    @GET("/comic/pustaka/{page}")
    suspend fun getPustaka(@Path("page") page: Int): PustakaWrapper

    @GET("/comic/scroll")
    suspend fun getScroll(): ScrollWrapper

    @GET("/comic/infinite")
    suspend fun getInfinite(): InfiniteWrapper

    @GET("/comic/search")
    suspend fun search(@Query("q") q: String): SearchWrapper

    @GET("/comic/comic/{slug}")
    suspend fun getDetail(@Path("slug") slug: String): MangaDetailDto

    @GET("/comic/chapter/{slug}")
    suspend fun getChapter(@Path("slug") slug: String): ChapterResponse

    @GET("/comic/chapter/{slug}/navigation")
    suspend fun getChapterNavigation(@Path("slug") slug: String): ChapterWrapper
}

fun createOkHttpClient(cache: Cache, rateLimit: RateLimitInterceptor, isDebug: Boolean = false): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(rateLimit)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .dispatcher(okhttp3.Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 15
        })
    if (isDebug) {
        builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
    }
    return builder.build()
}

fun createOkHttpClientForImages(cache: Cache, isDebug: Boolean = false): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(Block18Interceptor())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
    if (isDebug) {
        builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
    }
    return builder.build()
}

fun createRetrofit(client: OkHttpClient, json: Json): Retrofit {
    val contentType = "application/json".toMediaType()
    return Retrofit.Builder()
        .baseUrl("https://www.sankavollerei.web.id/")
        .client(client)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}
