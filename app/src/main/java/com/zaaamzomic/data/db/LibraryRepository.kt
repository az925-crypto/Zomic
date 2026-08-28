package com.zaaamzomic.data.db

import com.zaaamzomic.data.network.MangaDetailDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LibraryRepository(private val dao: MangaDao) {
    private val mutex = Mutex()
    fun observeLibrary(): Flow<List<MangaEntity>> = dao.observeLibrary()
    fun observeFiltered(reading: ReadingStatus?, pub: PublicationStatus?): Flow<List<MangaEntity>> =
        dao.observeFiltered(reading, pub)
    fun observeBySlug(slug: String): Flow<MangaEntity?> = dao.observeBySlug(slug)

    suspend fun bookmark(
        slug: String,
        title: String,
        thumbnail: String?,
        type: String?,
        genre: String?,
        description: String?,
        publicationStatus: PublicationStatus,
        publicationRaw: String?,
        readingStatus: ReadingStatus,
    ) = mutex.withLock {
        val now = System.currentTimeMillis()
        val existing = dao.getBySlug(slug)
        if (existing != null) {
            val safeThumb = thumbnail?.takeIf { it.startsWith("https://") }
            val safeDesc = description?.take(5000)
            val needsMetaUpdate = existing.title != title || existing.thumbnail != safeThumb || existing.type != type || existing.genre != genre || existing.description != safeDesc || existing.publicationStatus != publicationStatus
            if (needsMetaUpdate) {
                dao.upsert(existing.copy(title = title, thumbnail = safeThumb, type = type, genre = genre, description = safeDesc, publicationStatus = publicationStatus, publicationRaw = publicationRaw, readingStatus = readingStatus, updatedAt = now, isSourceUnavailable = false))
            } else {
                dao.updateReadingStatus(slug, readingStatus, now)
                if (existing.publicationStatus != publicationStatus) {
                    dao.syncPublicationStatus(slug, publicationStatus, publicationRaw, now)
                }
            }
            if (existing.isSourceUnavailable) dao.setSourceUnavailable(slug, false, now)
        } else {
            dao.upsert(
                MangaEntity(
                    slug = slug,
                    title = title,
                    thumbnail = thumbnail?.takeIf { it.startsWith("https://") },
                    type = type,
                    genre = genre,
                    description = description?.take(5000),
                    publicationStatus = publicationStatus,
                    publicationRaw = publicationRaw,
                    readingStatus = readingStatus,
                    lastReadChapterSlug = null,
                    lastReadChapterTitle = null,
                    lastReadPageIndex = 0,
                    totalPagesInChapter = null,
                    lastReadAt = null,
                    isSourceUnavailable = false,
                    addedAt = now,
                    updatedAt = now,
                )
            )
        }
    }

    suspend fun updateReadingStatus(slug: String, status: ReadingStatus) = mutex.withLock {
        dao.updateReadingStatus(slug, status, System.currentTimeMillis())
    }

    suspend fun syncPublication(slug: String, dto: MangaDetailDto) = mutex.withLock {
        val raw = dto.effectiveStatus
        val pub = parsePublication(raw, dto.effectiveDescription)
        val existing = dao.getBySlug(slug) ?: return@withLock
        val now = System.currentTimeMillis()
        dao.syncPublicationStatus(slug, pub, raw, now)
        if (existing.isSourceUnavailable) dao.setSourceUnavailable(slug, false, now)
        val safeThumb = dto.effectiveThumbnail?.takeIf { it.startsWith("https://") }
        val safeDesc = dto.effectiveDescription?.take(5000)
        val needsUpdate = existing.title != dto.title || existing.thumbnail != safeThumb || existing.type != dto.effectiveType || existing.genre != dto.effectiveGenre || existing.description != safeDesc || existing.publicationStatus != pub || existing.publicationRaw != raw
        if (needsUpdate) {
            dao.upsert(existing.copy(title = dto.title, thumbnail = safeThumb, type = dto.effectiveType, genre = dto.effectiveGenre, description = safeDesc, publicationStatus = pub, publicationRaw = raw, updatedAt = now, isSourceUnavailable = false))
        }
    }

    suspend fun markUnavailable(slug: String) = mutex.withLock {
        dao.setSourceUnavailable(slug, true, System.currentTimeMillis())
    }

    suspend fun saveProgress(mangaSlug: String, chapterSlug: String, chapterTitle: String?, pageIndex: Int, totalPages: Int?) = mutex.withLock {
        val now = System.currentTimeMillis()
        val existing = dao.getBySlug(mangaSlug)
        if (existing == null) {
            // create minimal entry so progress is not lost (upsert)
            dao.upsert(
                MangaEntity(
                    slug = mangaSlug,
                    title = mangaSlug,
                    thumbnail = null,
                    type = null,
                    genre = null,
                    description = null,
                    publicationStatus = PublicationStatus.UNKNOWN,
                    publicationRaw = null,
                    readingStatus = ReadingStatus.SEDANG_DIBACA,
                    lastReadChapterSlug = chapterSlug,
                    lastReadChapterTitle = chapterTitle,
                    lastReadPageIndex = pageIndex,
                    totalPagesInChapter = totalPages,
                    lastReadAt = now,
                    isSourceUnavailable = false,
                    addedAt = now,
                    updatedAt = now,
                )
            )
        } else {
            dao.saveProgress(mangaSlug, chapterSlug, chapterTitle, pageIndex, totalPages, now)
        }
    }

    companion object {
        fun parsePublication(statusRaw: String?, description: String?): PublicationStatus {
            val s = (statusRaw ?: "") + " " + (description ?: "")
            val lower = s.lowercase()
            return when {
                lower.contains("belum tamat") || lower.contains("ongoing") || lower.contains("berjalan") -> PublicationStatus.BELUM_TAMAT
                Regex("""\btamat\b""").containsMatchIn(lower) || Regex("""\bend\b""").containsMatchIn(lower) || Regex("""\bcompleted\b""").containsMatchIn(lower) || Regex("""\bfinished\b""").containsMatchIn(lower) || Regex("""\bcomplete\b""").containsMatchIn(lower) -> PublicationStatus.TAMAT
                else -> PublicationStatus.UNKNOWN
            }
        }
    }
}
