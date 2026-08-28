package com.zaaamzomic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaaamzomic.AppContainer
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.db.ReadingStatus
import com.zaaamzomic.ui.components.BookmarkSheet
import com.zaaamzomic.ui.components.MangaSpineCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    container: AppContainer,
    onMangaClick: (String) -> Unit,
    onContinueReading: (mangaSlug: String, chapterSlug: String, page: Int) -> Unit,
) {
    var readingFilter by remember { mutableStateOf<ReadingStatus?>(null) }
    var pubFilter by remember { mutableStateOf<PublicationStatus?>(null) }
    val scope = rememberCoroutineScope()
    var sheetSlug by remember { mutableStateOf<String?>(null) }

    val flow = remember(readingFilter, pubFilter) { container.libraryRepository.observeFiltered(readingFilter, pubFilter) }
    val items by flow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Library • Auto Organizer") }) }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            // filter chips
            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = readingFilter == null, onClick = { readingFilter = null }, label = { Text("Semua") })
                ReadingStatus.entries.forEach { rs ->
                    val label = when (rs) {
                        ReadingStatus.SEDANG_DIBACA -> "Sedang Dibaca"
                        ReadingStatus.BELUM_DIBACA -> "Belum Dibaca"
                        ReadingStatus.DROPPED -> "Dropped"
                    }
                    FilterChip(selected = readingFilter == rs, onClick = { readingFilter = if (readingFilter == rs) null else rs }, label = { Text(label) })
                }
            }
            Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = pubFilter == PublicationStatus.TAMAT, onClick = { pubFilter = if (pubFilter == PublicationStatus.TAMAT) null else PublicationStatus.TAMAT }, label = { Text("Tamat") })
                FilterChip(selected = pubFilter == PublicationStatus.BELUM_TAMAT, onClick = { pubFilter = if (pubFilter == PublicationStatus.BELUM_TAMAT) null else PublicationStatus.BELUM_TAMAT }, label = { Text("Belum Tamat") })
            }

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("Belum ada yang di-${readingFilter?.let { when(it){ReadingStatus.SEDANG_DIBACA->"Sedang Dibaca"; ReadingStatus.BELUM_DIBACA->"Belum Dibaca"; else->"Dropped"} } ?: "Library"}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Library & riwayat tetap bisa dibuka offline", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { /* nav to Terbaru via callback */ }) { Text("Jelajahi Terbaru") }
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items, key = { it.slug }) { manga ->
                        val progress = if (manga.totalPagesInChapter != null && manga.totalPagesInChapter > 0) {
                            (manga.lastReadPageIndex + 1).toFloat() / manga.totalPagesInChapter
                        } else null
                        MangaSpineCard(
                            title = manga.title,
                            thumbnail = manga.thumbnail,
                            type = manga.type,
                            genre = manga.genre,
                            chapterInfo = manga.lastReadChapterSlug?.let { "Terakhir: $it • P.${manga.lastReadPageIndex}" },
                            description = if (manga.isSourceUnavailable) "Sumber tidak tersedia" else manga.description,
                            publicationStatus = manga.publicationStatus,
                            readingStatus = manga.readingStatus,
                            progress = progress,
                            onClick = {
                                // FR-6: if sedang dibaca and has progress -> langsung reader
                                if (manga.readingStatus == ReadingStatus.SEDANG_DIBACA && manga.lastReadChapterSlug != null) {
                                    onContinueReading(manga.slug, manga.lastReadChapterSlug, manga.lastReadPageIndex)
                                } else {
                                    onMangaClick(manga.slug)
                                }
                            },
                        )
                        // long-press handled via sheet button (simplified: tap card shows sheet)
                        TextButton(onClick = { sheetSlug = manga.slug }) { Text("Pindah status") }
                    }
                }
            }
        }
        sheetSlug?.let { slug ->
            val manga = items.find { it.slug == slug }
            BookmarkSheet(
                title = manga?.title ?: slug,
                current = manga?.readingStatus,
                onSelect = { newStatus ->
                    scope.launch { container.libraryRepository.updateReadingStatus(slug, newStatus) }
                },
                onDismiss = { sheetSlug = null }
            )
        }
    }
}
