package com.zaaamzomic.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("""
        SELECT * FROM manga_library
        WHERE (:readingFilter IS NULL OR readingStatus = :readingFilter)
          AND (:pubFilter IS NULL OR publicationStatus = :pubFilter)
        ORDER BY updatedAt DESC
    """)
    fun observeFiltered(
        readingFilter: ReadingStatus?,
        pubFilter: PublicationStatus?,
    ): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga_library WHERE readingStatus IS NOT NULL ORDER BY updatedAt DESC")
    fun observeLibrary(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga_library WHERE slug = :slug")
    fun observeBySlug(slug: String): Flow<MangaEntity?>

    @Query("SELECT * FROM manga_library WHERE slug = :slug")
    suspend fun getBySlug(slug: String): MangaEntity?

    @Upsert
    suspend fun upsert(entity: MangaEntity)

    @Query("UPDATE manga_library SET readingStatus = :status, updatedAt = :now WHERE slug = :slug")
    suspend fun updateReadingStatus(slug: String, status: ReadingStatus?, now: Long)

    @Query("UPDATE manga_library SET publicationStatus = :pub, publicationRaw = :raw, updatedAt = :now WHERE slug = :slug")
    suspend fun syncPublicationStatus(slug: String, pub: PublicationStatus, raw: String?, now: Long)

    @Query("UPDATE manga_library SET isSourceUnavailable = :unavailable, updatedAt = :now WHERE slug = :slug")
    suspend fun setSourceUnavailable(slug: String, unavailable: Boolean, now: Long)

    @Query("""
        UPDATE manga_library SET
            lastReadChapterSlug = :chapterSlug,
            lastReadChapterTitle = :chapterTitle,
            lastReadPageIndex = :pageIndex,
            totalPagesInChapter = :totalPages,
            lastReadAt = :now,
            updatedAt = :now
        WHERE slug = :mangaSlug
    """)
    suspend fun saveProgress(
        mangaSlug: String,
        chapterSlug: String,
        chapterTitle: String?,
        pageIndex: Int,
        totalPages: Int?,
        now: Long,
    )

    @Query("DELETE FROM manga_library WHERE slug = :slug")
    suspend fun delete(slug: String)
}
