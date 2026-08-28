package com.zaaamzomic.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Helper to extract slug from link like "/manga/foo/" or "/detail-komik/foo/"
fun extractSlug(link: String?): String {
    if (link.isNullOrBlank()) return ""
    val trimmed = link.trim('/', ' ')
    if (trimmed.isEmpty()) return ""
    val last = trimmed.substringAfterLast('/')
    return last.ifBlank { trimmed.substringAfterLast('/', trimmed) }.trim()
}

// ========== Search ==========
@Serializable
data class MangaSummaryDto(
    val title: String = "",
    val slug: String = "",
    val href: String? = null,
    val thumbnail: String? = null,
    val type: String? = null,
    val genre: String? = null,
    val description: String? = null,
    val altTitle: String? = null,
)

// ========== Terbaru (real API returns comics array, not data) ==========
@Serializable
data class TerbaruComicDto(
    val title: String = "",
    val link: String? = null,
    val image: String? = null,
    val chapter: String? = null,
    @SerialName("time_ago") val timeAgo: String? = null,
    val status: String? = null,
    val rating: String? = null,
    val genre: String? = null,
    val views: JsonElement? = null,
    @SerialName("trending_score") val trendingScore: Int? = null,
    val timeframe: String? = null,
    @SerialName("scroll_position") val scrollPosition: Int? = null,
    val type: String? = null,
) {
    fun toSummary(): MangaSummaryDto {
        val s = extractSlug(link)
        return MangaSummaryDto(
            title = title,
            slug = s,
            href = link,
            thumbnail = image,
            type = type,
            genre = genre,
            description = listOfNotNull(chapter, timeAgo, genre).filter { it.isNotBlank() }.joinToString(" • "),
        )
    }
}

@Serializable
data class TerbaruWrapper(
    val creator: String? = null,
    val comics: List<TerbaruComicDto> = emptyList(),
    val data: List<MangaSummaryDto>? = null,
    val pagination: JsonElement? = null,
)

// ========== Trending ==========
@Serializable
data class TrendingWrapper(
    val creator: String? = null,
    val trending: List<TerbaruComicDto> = emptyList(),
    val timeframe: String? = null,
    val count: Int? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
)

// ========== Genres list ==========
@Serializable
data class GenreValueDto(
    val value: String = "",
    val name: String = "",
)

@Serializable
data class GenresWrapper(
    val creator: String? = null,
    val status: JsonElement? = null,
    val message: String? = null,
    val data: List<GenreValueDto> = emptyList(),
)

// Genre comics: /comic/genre/{value}
@Serializable
data class GenreComicsWrapper(
    val creator: String? = null,
    val genre: String? = null,
    val comics: List<TerbaruComicDto> = emptyList(),
    val pagination: JsonElement? = null,
    val metadata: JsonElement? = null,
)

// Pustaka / Berwarna results have different shape
@Serializable
data class PustakaDto(
    val title: String = "",
    val thumbnail: String? = null,
    val type: String? = null,
    val genre: String? = null,
    val url: String? = null,
    @SerialName("detailUrl") val detailUrl: String? = null,
    val description: String? = null,
    val stats: String? = null,
) {
    fun toSummary(): MangaSummaryDto {
        val s = extractSlug(detailUrl ?: url)
        return MangaSummaryDto(
            title = title,
            slug = s,
            href = detailUrl ?: url,
            thumbnail = thumbnail,
            type = type,
            genre = genre,
            description = listOfNotNull(description?.take(60), stats).joinToString(" • "),
        )
    }
}

@Serializable
data class PustakaWrapper(
    val creator: String? = null,
    val page: Int? = null,
    val results: List<PustakaDto> = emptyList(),
)

@Serializable
data class BerwarnaDataDto(
    val page: Int? = null,
    val results: List<PustakaDto> = emptyList(),
)

@Serializable
data class BerwarnaWrapper(
    val creator: String? = null,
    val status: JsonElement? = null,
    val message: String? = null,
    val data: BerwarnaDataDto? = null,
)

// Infinite / Scroll
@Serializable
data class InfiniteWrapper(
    val creator: String? = null,
    val type: String? = null,
    val comics: List<TerbaruComicDto> = emptyList(),
    val pagination: JsonElement? = null,
)

@Serializable
data class ScrollWrapper(
    val creator: String? = null,
    val comics: List<TerbaruComicDto> = emptyList(),
    @SerialName("scroll_info") val scrollInfo: JsonElement? = null,
)

// ========== Detail (real API returns direct object, no wrapper) ==========
@Serializable
data class MetadataDto(
    val type: String? = null,
    val author: String? = null,
    val status: String? = null,
    val concept: String? = null,
    @SerialName("age_rating") val ageRating: String? = null,
)

@Serializable
data class GenreDto(
    val name: String = "",
    val slug: String = "",
    val link: String? = null,
)

@Serializable
data class ChapterInfoDto(
    val title: String = "",
    val slug: String = "",
    val href: String? = null,
    val date: String? = null,
    val chapter: String? = null,
    val link: String? = null,
) {
    val displayTitle: String get() = title.ifBlank { chapter ?: slug }
    val effectiveSlug: String get() = slug.ifBlank { extractSlug(link ?: href) }
}

@Serializable
data class MangaDetailDto(
    val title: String = "",
    val slug: String = "",
    val image: String? = null,
    val thumbnail: String? = null,
    val type: String? = null,
    val genre: String? = null,
    val description: String? = null,
    val synopsis: String? = null,
    @SerialName("synopsis_full") val synopsisFull: String? = null,
    val status: String? = null,
    val author: String? = null,
    val metadata: MetadataDto? = null,
    val genres: List<GenreDto>? = null,
    val chapters: List<ChapterInfoDto> = emptyList(),
    @SerialName("chapter_list") val chapterList: List<ChapterInfoDto>? = null,
    @SerialName("chapterList") val altChapterList: List<ChapterInfoDto>? = null,
) {
    val effectiveThumbnail: String? get() = thumbnail ?: image
    val effectiveDescription: String? get() = synopsis ?: synopsisFull ?: description
    val effectiveStatus: String? get() = status ?: metadata?.status
    val effectiveType: String? get() = type ?: metadata?.type
    val effectiveGenre: String? get() = genre ?: metadata?.concept ?: genres?.joinToString(", ") { it.name }
    val effectiveAuthor: String? get() = author ?: metadata?.author
    val effectiveChapters: List<ChapterInfoDto> get() = when {
        chapters.isNotEmpty() -> chapters
        chapterList != null && chapterList.isNotEmpty() -> chapterList
        altChapterList != null && altChapterList.isNotEmpty() -> altChapterList
        else -> emptyList()
    }
}

@Serializable
data class ChapterDto(
    val title: String = "",
    val slug: String = "",
    val images: List<String> = emptyList(),
    @SerialName("image") val image: String? = null,
    @SerialName("imageList") val imageList: List<String>? = null,
    @SerialName("chapter_images") val chapterImages: List<String>? = null,
    @SerialName("manga_title") val mangaTitle: String? = null,
    @SerialName("chapter_title") val chapterTitle: String? = null,
    val navigation: NavigationDto? = null,
)

@Serializable
data class NavigationDto(
    @SerialName("previousChapter") val previousChapter: String? = null,
    @SerialName("nextChapter") val nextChapter: String? = null,
    @SerialName("chapterList") val chapterList: String? = null,
)

@Serializable
data class ChapterResponse(
    val creator: String? = null,
    @SerialName("manga_title") val mangaTitle: String? = null,
    @SerialName("chapter_title") val chapterTitle: String? = null,
    val title: String? = null,
    val slug: String? = null,
    val images: List<String> = emptyList(),
    val navigation: NavigationDto? = null,
    val data: ChapterDto? = null,
) {
    val effectiveImages: List<String> get() = when {
        images.isNotEmpty() -> images
        data?.images?.isNotEmpty() == true -> data.images
        data?.imageList?.isNotEmpty() == true -> data.imageList!!
        data?.chapterImages?.isNotEmpty() == true -> data.chapterImages!!
        data?.image != null -> listOf(data.image)
        else -> emptyList()
    }
    val effectiveTitle: String get() = chapterTitle ?: title ?: data?.title ?: ""
}

// Wrappers for search
@Serializable
data class SearchWrapper(
    val status: JsonElement? = null,
    val message: String? = null,
    val total: Int? = null,
    val data: List<MangaSummaryDto> = emptyList(),
)

@Serializable
data class DetailWrapper(
    val status: JsonElement? = null,
    val data: MangaDetailDto? = null,
    @SerialName("statusCode") val statusCode: Int? = null,
)

@Serializable
data class ChapterWrapper(
    val status: JsonElement? = null,
    val data: ChapterDto? = null,
)
