package com.zaaamzomic.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Wrapper is inconsistent: {status: true/false OR "success", data: ...}
@Serializable
data class MangaSummaryDto(
    val title: String = "",
    val slug: String = "",
    val href: String? = null,
    val thumbnail: String? = null,
    val type: String? = null,
    val genre: String? = null,
    val description: String? = null,
)

@Serializable
data class MangaDetailDto(
    val title: String = "",
    val slug: String = "",
    val thumbnail: String? = null,
    val type: String? = null,
    val genre: String? = null,
    val description: String? = null,
    val status: String? = null, // Tamat / Ongoing / Completed etc
    val author: String? = null,
    val chapters: List<ChapterInfoDto> = emptyList(),
    // fallback fields: some responses embed chapters differently
    @SerialName("chapter_list") val chapterList: List<ChapterInfoDto>? = null,
    @SerialName("chapterList") val altChapterList: List<ChapterInfoDto>? = null,
)

@Serializable
data class ChapterInfoDto(
    val title: String = "",
    val slug: String = "",
    val href: String? = null,
    val date: String? = null,
)

@Serializable
data class ChapterDto(
    val title: String = "",
    val slug: String = "",
    val images: List<String> = emptyList(),
    // Some APIs return {data: {images: []}} or {data: []}
    @SerialName("image") val image: String? = null,
    @SerialName("imageList") val imageList: List<String>? = null,
    @SerialName("chapter_images") val chapterImages: List<String>? = null,
)

// Search wrapper: {status: true OR "success", data: []} -> tolerant via JsonElement
@Serializable
data class SearchWrapper(
    val status: JsonElement? = null,
    val message: String? = null,
    val total: Int? = null,
    val data: List<MangaSummaryDto> = emptyList(),
)

// Detail wrapper variations
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
