package com.zaaamzomic.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PublicationStatus { TAMAT, BELUM_TAMAT, UNKNOWN }
enum class ReadingStatus { SEDANG_DIBACA, BELUM_DIBACA, DROPPED }

@Entity(tableName = "manga_library")
data class MangaEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val thumbnail: String?,
    val type: String?,
    val genre: String?,
    val description: String?,
    val publicationStatus: PublicationStatus,
    val publicationRaw: String?,
    val readingStatus: ReadingStatus?,
    val lastReadChapterSlug: String?,
    val lastReadChapterTitle: String?,
    val lastReadPageIndex: Int = 0,
    val totalPagesInChapter: Int? = null,
    val lastReadAt: Long? = null,
    val isSourceUnavailable: Boolean = false,
    val addedAt: Long,
    val updatedAt: Long,
)
