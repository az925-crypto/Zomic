package com.zaaamzomic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.zaaamzomic.AppContainer
import com.zaaamzomic.data.db.LibraryRepository
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.db.ReadingStatus
import com.zaaamzomic.data.network.ChapterInfoDto
import com.zaaamzomic.data.network.MangaDetailDto
import com.zaaamzomic.ui.components.BookmarkSheet
import com.zaaamzomic.ui.theme.*
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
                val dto = container.sankaService.getDetail(slug)
                if (dto.title.isNotBlank() || dto.slug.isNotBlank()) {
                    detail = dto
                    chapters = dto.effectiveChapters
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
            val raw = d.effectiveStatus
            val desc = d.effectiveDescription
            val pub = LibraryRepository.parsePublication(raw, desc)
            container.libraryRepository.bookmark(
                slug = slug,
                title = d.title,
                thumbnail = d.effectiveThumbnail,
                type = d.effectiveType,
                genre = d.effectiveGenre,
                description = desc,
                publicationStatus = pub,
                publicationRaw = raw,
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

    Scaffold(containerColor = PaperIvory) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().background(PaperIvory)) {
            when {
                vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Hanko) }
                vm.error != null && vm.detail == null -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(vm.error ?: "Error", fontWeight = FontWeight.Bold, color = Sumi)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = Hanko)) { Text("Coba Lagi", color = Color.White) }
                    if (vm.error?.contains("Sumber tidak tersedia") == true) {
                        Spacer(Modifier.height(12.dp))
                        Text("Tetap muncul di Library dengan tag sumber tidak tersedia", style = MaterialTheme.typography.bodySmall, color = GalleyGrey)
                    }
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    item {
                        val d = vm.detail
                        if (d != null) {
                            val pub = LibraryRepository.parsePublication(d.effectiveStatus, d.effectiveDescription)
                            val isTamat = pub == PublicationStatus.TAMAT
                            // Hero 300dp like mockup
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(Sumi)
                            ) {
                                // cover as background
                                AsyncImage(
                                    model = d.effectiveThumbnail,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.45f
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0x660F1720), Color(0xF20F1720))
                                        )
                                    )
                                )
                                // top overlay buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp).align(Alignment.TopCenter),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    FilledTonalButton(onClick = onBack, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.Black.copy(alpha = 0.35f), contentColor = Color.White), shape = RoundedCornerShape(10.dp)) { Text("‹ Kembali", fontSize = 12.sp) }
                                    FilledTonalButton(onClick = { vm.showBookmark = true }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.Black.copy(alpha = 0.35f), contentColor = Color.White), shape = RoundedCornerShape(10.dp)) { Text("Bookmark", fontSize = 12.sp) }
                                }
                                Column(
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = d.effectiveThumbnail,
                                        contentDescription = d.title,
                                        modifier = Modifier.size(width = 120.dp, height = 160.dp).clip(RoundedCornerShape(10.dp)).background(SpineMist2),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(d.title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White, lineHeight = 24.sp)
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val badgeText = if (isTamat) "● TAMAT" else "○ ONGOING"
                                        val badgeBg = if (isTamat) OkGreen else Color.White
                                        val badgeCol = if (isTamat) Color.White else Sumi
                                        Text(badgeText, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 3.dp), color = badgeCol)
                                        Text("${d.effectiveType ?: "Manga"} • ${d.effectiveGenre ?: ""} • ${vm.chapters.size} Ch", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                    }
                                }
                            }
                            // Ledger like mockup
                            Card(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SpineMist),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Outline)
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("PROGRESS LEDGER", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Sumi)
                                        Text("— — — ● — — —", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Hanko)
                                    }
                                    Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(999.dp)).background(SpineMist2)) {
                                        val p = prog
                                        val progPct = if (p?.totalPagesInChapter != null && p.totalPagesInChapter > 0) (p.lastReadPageIndex + 1).toFloat() / p.totalPagesInChapter else 0.62f
                                        Box(Modifier.fillMaxHeight().fillMaxWidth(progPct).background(if (isTamat) OkGreen else Hanko))
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        val p = prog
                                        Text(p?.let { "Terakhir: ${it.lastReadChapterSlug} • P.${it.lastReadPageIndex}" } ?: "Belum ada progress", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey)
                                        Text(if (isTamat) "92%" else "62%", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Sumi)
                                    }
                                    Button(
                                        onClick = {
                                            val p = prog
                                            val last = p?.lastReadChapterSlug
                                            if (last != null) onChapterClick(last) else if (vm.chapters.isNotEmpty()) onChapterClick(vm.chapters.first().effectiveSlug)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isTamat) OkGreen else Hanko),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        val p = prog
                                        Text(if (p?.lastReadChapterSlug != null) "Lanjut Baca ${p.lastReadChapterSlug}" else "Mulai Baca Ch. 1", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { vm.showBookmark = true }, modifier = Modifier.weight(1f), border = androidx.compose.foundation.BorderStroke(1.dp, Outline)) { Text("Bookmark", color = Sumi) }
                                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), border = androidx.compose.foundation.BorderStroke(1.dp, Outline)) { Text("Bagikan", color = Sumi) }
                                    }
                                    Text("FR-5 • simpan posisi chapter+halaman • < 3 tap untuk lanjut", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = GalleyGrey, modifier = Modifier.align(Alignment.CenterHorizontally))
                                }
                            }
                            // Synopsis
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                Text(d.effectiveDescription ?: "", fontSize = 13.sp, lineHeight = 20.sp, color = Sumi, maxLines = 4)
                                Text("Selengkapnya", color = Hanko, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            // unavailable banner
                            if (prog.let { it?.isSourceUnavailable == true }) {
                                Card(
                                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8C28A)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("⚠", fontSize = 18.sp)
                                        Column {
                                            Text("Sumber tidak tersedia", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF9A6B1A))
                                            Text("Manga hilang dari API, tapi tetap muncul di Library & history tersimpan.", fontSize = 11.sp, color = Color(0xFF6B5900))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                    if (vm.chapters.isNotEmpty()) {
                        item {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("DAFTAR CHAPTER • ${vm.chapters.size}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 0.6.sp, color = Sumi)
                                Text("Terbaru di atas", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey)
                            }
                        }
                        itemsIndexed(vm.chapters, key = { idx, ch -> "${ch.effectiveSlug}_${ch.displayTitle}_$idx" }) { _, ch ->
                            val isLast = prog?.lastReadChapterSlug == ch.effectiveSlug
                            Row(
                                modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onChapterClick(ch.effectiveSlug) }.padding(horizontal = 16.dp).background(if (isLast) HankoBg else Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(ch.displayTitle, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Sumi)
                                        if (isLast) Text("TERAKHIR DIBACA", fontSize = 10.sp, color = Color.White, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Hanko).padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Text(ch.date ?: "", fontSize = 11.sp, color = GalleyGrey)
                                }
                                Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(if (isLast) Hanko else Color.Transparent).background(if (isLast) Hanko else Color(0xFFDDD6C3)))
                            }
                            HorizontalDivider(color = Outline, thickness = 0.5.dp)
                        }
                        item {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) {
                                Text("GET /comic/comic/${vm.detail?.slug ?: slug}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey, modifier = Modifier.background(SpineMist, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
                            }
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
