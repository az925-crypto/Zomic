package com.zaaamzomic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zaaamzomic.AppContainer
import com.zaaamzomic.data.db.LibraryRepository
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.db.ReadingStatus
import com.zaaamzomic.data.network.ChapterInfoDto
import com.zaaamzomic.data.network.MangaDetailDto
import com.zaaamzomic.ui.components.BookmarkSheet
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DetailViewModel(private val container: AppContainer, private val slug: String) : ViewModel() {
    var detail by mutableStateOf<MangaDetailDto?>(null)
        private set
    var chapters by mutableStateOf<List<ChapterInfoDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var showBookmark by mutableStateOf(false)

    val progress by lazy { container.libraryRepository.observeBySlug(slug) }

    fun load() {
        viewModelScope.launch {
            loading = true; error = null
            try {
                val res = container.sankaService.getDetail(slug)
                val dto = res.data
                if (dto != null) {
                    detail = dto
                    chapters = dto.chapters.ifEmpty { dto.chapterList ?: dto.altChapterList ?: emptyList() }
                    // sync publication
                    container.libraryRepository.syncPublication(slug, dto)
                } else {
                    error = "Data kosong"
                }
            } catch (e: Exception) {
                val is404 = (e is HttpException && e.code() == 404) || e.message?.contains("404") == true
                if (is404) {
                    container.libraryRepository.markUnavailable(slug)
                    error = "Sumber tidak tersedia — tapi history tetap tersimpan"
                } else {
                    error = e.message ?: "Gagal memuat"
                }
            } finally { loading = false }
        }
    }

    fun bookmark(status: ReadingStatus) {
        val d = detail ?: return
        viewModelScope.launch {
            val pub = LibraryRepository.parsePublication(d.status, d.description)
            container.libraryRepository.bookmark(
                slug = slug,
                title = d.title,
                thumbnail = d.thumbnail,
                type = d.type,
                genre = d.genre,
                description = d.description,
                publicationStatus = pub,
                publicationRaw = d.status,
                readingStatus = status,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    container: AppContainer,
    slug: String,
    onChapterClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val vm: DetailViewModel = viewModel(key = slug, factory = viewModelFactory { initializer { DetailViewModel(container, slug) } })
    LaunchedEffect(slug) { vm.load() }
    val prog by vm.progress.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vm.detail?.title ?: slug) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ Kembali") }
                },
                actions = {
                    TextButton(onClick = { vm.showBookmark = true }) { Text("Bookmark") }
                }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when {
                vm.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                vm.error != null && vm.detail == null -> Column(Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(vm.error ?: "Error")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.load() }) { Text("Coba Lagi") }
                    if (vm.error?.contains("Sumber tidak tersedia") == true) {
                        Spacer(Modifier.height(8.dp))
                        Text("Tetap muncul di Library dengan tag sumber tidak tersedia", style = MaterialTheme.typography.bodySmall)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        vm.detail?.let { d ->
                            val pub = LibraryRepository.parsePublication(d.status, d.description)
                            val pubLabel = when (pub) {
                                PublicationStatus.TAMAT -> "● TAMAT"
                                PublicationStatus.BELUM_TAMAT -> "○ ONGOING"
                                PublicationStatus.UNKNOWN -> "Status tak diketahui"
                            }
                            Card {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(d.title, style = MaterialTheme.typography.titleLarge)
                                    Text(pubLabel, style = MaterialTheme.typography.labelSmall, color = if (pub == PublicationStatus.TAMAT) com.zaaamzomic.ui.theme.OkGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(d.description ?: "", style = MaterialTheme.typography.bodyMedium)
                                    if (prog?.lastReadChapterSlug != null) {
                                        HorizontalDivider()
                                        Text("Terakhir: ${prog?.lastReadChapterSlug} • P.${prog?.lastReadPageIndex ?: 0}", style = MaterialTheme.typography.labelSmall)
                                        Button(onClick = { prog?.lastReadChapterSlug?.let(onChapterClick) }, modifier = Modifier.fillMaxWidth()) {
                                            Text("Lanjut Baca ${prog?.lastReadChapterSlug}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (vm.chapters.isNotEmpty()) {
                        item { Text("Daftar Chapter • ${vm.chapters.size}", style = MaterialTheme.typography.titleMedium) }
                        itemsIndexed(vm.chapters, key = { idx, ch -> "${ch.slug}_${ch.title}_$idx" }) { _, ch ->
                            ListItem(
                                headlineContent = { Text(ch.title.ifBlank { ch.slug }) },
                                supportingContent = { Text(ch.date ?: "") },
                                trailingContent = {
                                    val isRead = prog?.lastReadChapterSlug == ch.slug
                                    if (isRead) Badge { Text("Terakhir") }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { onChapterClick(ch.slug) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
            if (vm.showBookmark) {
                BookmarkSheet(
                    title = vm.detail?.title ?: slug,
                    current = prog?.readingStatus,
                    onSelect = vm::bookmark,
                    onDismiss = { vm.showBookmark = false }
                )
            }
        }
    }
}
