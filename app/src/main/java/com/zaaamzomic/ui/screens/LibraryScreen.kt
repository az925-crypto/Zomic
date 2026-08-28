package com.zaaamzomic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaaamzomic.AppContainer
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.db.ReadingStatus
import com.zaaamzomic.ui.components.BookmarkSheet
import com.zaaamzomic.ui.components.MangaSpineCard
import com.zaaamzomic.ui.theme.*
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
    val allItems by container.libraryRepository.observeLibrary().collectAsState(initial = emptyList())

    // counts for chips
    val sedangCount = allItems.count { it.readingStatus == ReadingStatus.SEDANG_DIBACA }
    val belumCount = allItems.count { it.readingStatus == ReadingStatus.BELUM_DIBACA }
    val droppedCount = allItems.count { it.readingStatus == ReadingStatus.DROPPED }

    Scaffold(
        containerColor = PaperIvory,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperIvory, titleContentColor = Sumi),
                title = {
                    Column {
                        Text("ZOMIC", fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.2.sp, color = Sumi)
                        Text("LIBRARY • AUTO ORGANIZER", fontFamily = FontFamily.Monospace, fontSize = 8.sp, letterSpacing = 1.4.sp, color = GalleyGrey)
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(PaperIvory)) {
            // filter row 2 dimensi like mockup
            Row(
                modifier = Modifier.fillMaxWidth().background(PaperIvory).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = readingFilter == null,
                    onClick = { readingFilter = null },
                    label = { Text("Semua", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Hanko, selectedLabelColor = PaperIvory, containerColor = PaperIvory),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Outline, selectedBorderColor = Hanko, enabled = true, selected = readingFilter == null)
                )
                FilterChip(
                    selected = readingFilter == ReadingStatus.SEDANG_DIBACA,
                    onClick = { readingFilter = if (readingFilter == ReadingStatus.SEDANG_DIBACA) null else ReadingStatus.SEDANG_DIBACA },
                    label = { Text("Sedang Dibaca • $sedangCount", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Hanko, selectedLabelColor = PaperIvory, containerColor = PaperIvory),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Outline, selectedBorderColor = Hanko, enabled = true, selected = readingFilter == ReadingStatus.SEDANG_DIBACA)
                )
                FilterChip(
                    selected = readingFilter == ReadingStatus.BELUM_DIBACA,
                    onClick = { readingFilter = if (readingFilter == ReadingStatus.BELUM_DIBACA) null else ReadingStatus.BELUM_DIBACA },
                    label = { Text("Belum Dibaca • $belumCount", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Hanko, selectedLabelColor = PaperIvory, containerColor = PaperIvory),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Outline, selectedBorderColor = Hanko, enabled = true, selected = readingFilter == ReadingStatus.BELUM_DIBACA)
                )
                FilterChip(
                    selected = readingFilter == ReadingStatus.DROPPED,
                    onClick = { readingFilter = if (readingFilter == ReadingStatus.DROPPED) null else ReadingStatus.DROPPED },
                    label = { Text("Dropped • $droppedCount", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Hanko, selectedLabelColor = PaperIvory, containerColor = PaperIvory),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Outline, selectedBorderColor = Hanko, enabled = true, selected = readingFilter == ReadingStatus.DROPPED)
                )
                Box(Modifier.width(1.dp).height(16.dp).background(Outline))
                FilterChip(
                    selected = pubFilter == PublicationStatus.TAMAT,
                    onClick = { pubFilter = if (pubFilter == PublicationStatus.TAMAT) null else PublicationStatus.TAMAT },
                    label = { Text("Tamat", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Hanko, selectedLabelColor = PaperIvory, containerColor = PaperIvory),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Outline, selectedBorderColor = Hanko, enabled = true, selected = pubFilter == PublicationStatus.TAMAT)
                )
                FilterChip(
                    selected = pubFilter == PublicationStatus.BELUM_TAMAT,
                    onClick = { pubFilter = if (pubFilter == PublicationStatus.BELUM_TAMAT) null else PublicationStatus.BELUM_TAMAT },
                    label = { Text("Belum Tamat", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Hanko, selectedLabelColor = PaperIvory, containerColor = PaperIvory),
                    border = FilterChipDefaults.filterChipBorder(borderColor = Outline, selectedBorderColor = Hanko, enabled = true, selected = pubFilter == PublicationStatus.BELUM_TAMAT)
                )
            }
            HorizontalDivider(color = Outline)

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize().background(PaperIvory), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        // empty illustration like mockup
                        Box(Modifier.size(120.dp, 80.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(12.dp)) {
                            Box(Modifier.size(36.dp, 48.dp).clip(RoundedCornerShape(4.dp)).background(SpineMist).align(Alignment.CenterStart))
                            Box(Modifier.size(40.dp, 52.dp).clip(RoundedCornerShape(4.dp)).background(Color.White).align(Alignment.Center))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Belum ada yang di-${readingFilter?.let { when(it){ReadingStatus.SEDANG_DIBACA->"Sedang Dibaca"; ReadingStatus.BELUM_DIBACA->"Belum Dibaca"; else->"Dropped"} } ?: "Library"}", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Sumi)
                        Spacer(Modifier.height(6.dp))
                        Text("Library kamu masih kosong. Mulai dari Terbaru, bookmark manga biar auto-keorganize.", fontSize = 12.sp, color = GalleyGrey)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Hanko), shape = RoundedCornerShape(999.dp)) { Text("Jelajahi Terbaru", color = Color.White) }
                        Spacer(Modifier.height(12.dp))
                        Text("Offline parsial: Library & riwayat tetap bisa dibuka saat API down.", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey, modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            } else {
                // Group by status like mockup
                val sedang = items.filter { it.readingStatus == ReadingStatus.SEDANG_DIBACA }
                val belum = items.filter { it.readingStatus == ReadingStatus.BELUM_DIBACA }
                val dropped = items.filter { it.readingStatus == ReadingStatus.DROPPED }
                val others = items.filter { it.readingStatus == null }

                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.background(PaperIvory)) {
                    if (sedang.isNotEmpty()) {
                        item {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SEDANG DIBACA (${sedang.size})", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp, color = Sumi)
                                Text("tap → langsung Reader", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey)
                            }
                        }
                        items(sedang, key = { it.slug }) { manga ->
                            val progress = if (manga.totalPagesInChapter != null && manga.totalPagesInChapter > 0) (manga.lastReadPageIndex + 1).toFloat() / manga.totalPagesInChapter else null
                            Column {
                                MangaSpineCard(
                                    title = manga.title,
                                    thumbnail = manga.thumbnail,
                                    type = manga.type,
                                    genre = manga.genre,
                                    chapterInfo = manga.lastReadChapterSlug?.let { "Ch. ${it.substringAfterLast('-')} / ${manga.title}" },
                                    description = if (manga.isSourceUnavailable) "Sumber tidak tersedia" else manga.description,
                                    publicationStatus = manga.publicationStatus,
                                    readingStatus = manga.readingStatus,
                                    progress = progress,
                                    onClick = {
                                        if (manga.lastReadChapterSlug != null) onContinueReading(manga.slug, manga.lastReadChapterSlug, manga.lastReadPageIndex)
                                        else onMangaClick(manga.slug)
                                    }
                                )
                                TextButton(onClick = { sheetSlug = manga.slug }) { Text("Pindah status", fontSize = 12.sp, color = Hanko) }
                            }
                        }
                    }
                    if (belum.isNotEmpty()) {
                        item {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("BELUM DIBACA (${belum.size})", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp, color = Sumi)
                                Text("tap → Detail", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey)
                            }
                        }
                        items(belum, key = { it.slug }) { manga ->
                            MangaSpineCard(
                                title = manga.title,
                                thumbnail = manga.thumbnail,
                                type = manga.type,
                                genre = manga.genre,
                                chapterInfo = "Manga • ${if (manga.publicationStatus==PublicationStatus.TAMAT) "Tamat" else "Ongoing"} • ${manga.genre ?: ""}",
                                description = "Tamat, tinggal kamu selesaikan.",
                                publicationStatus = manga.publicationStatus,
                                readingStatus = manga.readingStatus,
                                onClick = { onMangaClick(manga.slug) }
                            )
                        }
                    }
                    if (dropped.isNotEmpty()) {
                        item { Text("DROPPED (${dropped.size})", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp, color = Sumi, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) }
                        items(dropped, key = { it.slug }) { manga ->
                            MangaSpineCard(
                                title = manga.title,
                                thumbnail = manga.thumbnail,
                                type = manga.type,
                                genre = manga.genre,
                                chapterInfo = "Dropped • swipe kanan untuk restore",
                                description = null,
                                publicationStatus = manga.publicationStatus,
                                readingStatus = manga.readingStatus,
                                onClick = { onMangaClick(manga.slug) }
                            )
                        }
                    }
                    if (others.isNotEmpty() && sedang.isEmpty() && belum.isEmpty() && dropped.isEmpty()) {
                        items(others, key = { it.slug }) { manga ->
                            MangaSpineCard(
                                title = manga.title,
                                thumbnail = manga.thumbnail,
                                type = manga.type,
                                genre = manga.genre,
                                chapterInfo = null,
                                description = manga.description,
                                publicationStatus = manga.publicationStatus,
                                readingStatus = manga.readingStatus,
                                onClick = { onMangaClick(manga.slug) }
                            )
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) {
                            Text("FR-9 filter: readingStatus × publicationStatus", fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.background(Sumi, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White)
                        }
                    }
                }
            }
        }
        sheetSlug?.let { slug ->
            val manga = items.find { it.slug == slug } ?: allItems.find { it.slug == slug }
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
