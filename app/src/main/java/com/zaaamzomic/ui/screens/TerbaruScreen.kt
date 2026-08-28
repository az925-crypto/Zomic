package com.zaaamzomic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zaaamzomic.AppContainer
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.network.GenreValueDto
import com.zaaamzomic.data.network.MangaSummaryDto
import com.zaaamzomic.ui.theme.*
import kotlinx.coroutines.launch

enum class HomeOrder(val label: String) { Terbaru("Terbaru"), Populer("Populer"), Trending("Trending"), Berwarna("Berwarna") }

class TerbaruViewModel(private val container: AppContainer) : ViewModel() {
    var items by mutableStateOf<List<MangaSummaryDto>>(emptyList())
        private set
    var trendingPreview by mutableStateOf<List<MangaSummaryDto>>(emptyList())
        private set
    var genres by mutableStateOf<List<GenreValueDto>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var selectedOrder by mutableStateOf(HomeOrder.Terbaru)
        private set
    var selectedGenre by mutableStateOf<String?>(null)
        private set

    fun loadGenres() {
        viewModelScope.launch {
            try {
                val res = container.sankaService.getGenres()
                if (res.data.isNotEmpty()) genres = res.data
            } catch (_: Exception) {}
        }
    }

    fun selectOrder(order: HomeOrder) {
        if (selectedOrder == order && selectedGenre == null) return
        selectedOrder = order
        selectedGenre = null
        load(force = true)
    }

    fun selectGenre(value: String?) {
        selectedGenre = value
        load(force = true)
    }

    fun load(force: Boolean = false) {
        if (loading && !force) return
        viewModelScope.launch {
            loading = true; error = null
            try {
                val mapped: List<MangaSummaryDto> = when {
                    selectedGenre != null -> {
                        val r = container.sankaService.getGenreComics(selectedGenre!!)
                        r.comics.map { it.toSummary() }
                    }
                    selectedOrder == HomeOrder.Terbaru -> {
                        val r = container.sankaService.getTerbaru()
                        r.comics.map { it.toSummary() }
                    }
                    selectedOrder == HomeOrder.Populer -> {
                        val r = container.sankaService.getPopuler()
                        r.comics.map { it.toSummary() }
                    }
                    selectedOrder == HomeOrder.Trending -> {
                        val r = container.sankaService.getTrending()
                        r.trending.map { it.toSummary() }
                    }
                    selectedOrder == HomeOrder.Berwarna -> {
                        val r = container.sankaService.getBerwarna(1)
                        r.data?.results?.map { it.toSummary() } ?: emptyList()
                    }
                    else -> emptyList()
                }
                items = mapped
                if (mapped.isEmpty()) error = "Kosong — coba ganti filter atau tarik untuk muat ulang"
                // preload trending strip for Terbaru tab
                if (trendingPreview.isEmpty() && selectedOrder == HomeOrder.Terbaru && selectedGenre == null) {
                    try {
                        val t = container.sankaService.getTrending()
                        trendingPreview = t.trending.take(8).map { it.toSummary() }
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Gagal memuat"
                error = if (msg.contains("429")) "Kena batas 30/menit — tunggu 30 detik lalu tarik untuk muat ulang" else msg
            } finally { loading = false; isRefreshing = false }
        }
    }

    fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        load(force = true)
    }

    fun retry() = load(force = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerbaruScreen(
    container: AppContainer,
    onMangaClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val vm: TerbaruViewModel = viewModel(factory = viewModelFactory { initializer { TerbaruViewModel(container) } })
    LaunchedEffect(Unit) { vm.loadGenres(); vm.load() }

    Scaffold(
        containerColor = PaperIvory,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperIvory, titleContentColor = Sumi),
                title = {
                    Column {
                        Text("ZOMIC", fontFamily = FontFamily.Serif, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.2.sp, color = Sumi)
                        Text("HOME • KOMIKU DISCOVERY", fontFamily = FontFamily.Monospace, fontSize = 8.sp, letterSpacing = 1.4.sp, color = GalleyGrey, fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(SpineMist)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang", tint = Sumi)
                    }
                    IconButton(onClick = onSearchClick, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(PaperIvory)) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = Sumi)
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(PaperIvory)) {
            // Row 1 — Order (Terbaru / Populer / Trending / Berwarna)
            Row(
                modifier = Modifier.fillMaxWidth().background(PaperIvory).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeOrder.entries.forEach { order ->
                    val active = vm.selectedOrder == order && vm.selectedGenre == null
                    FilterChip(
                        selected = active,
                        onClick = { vm.selectOrder(order) },
                        label = { Text(order.label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Hanko,
                            selectedLabelColor = PaperIvory,
                            containerColor = PaperIvory,
                            labelColor = Sumi
                        )
                    )
                }
                Box(Modifier.width(1.dp).height(16.dp).background(Outline))
                Text("tarik ↓ untuk muat ulang", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = GalleyGrey, modifier = Modifier.background(SpineMist, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            // Row 2 — Genre chips (from /comic/genres)
            Row(
                modifier = Modifier.fillMaxWidth().background(PaperIvory).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAllGenre = vm.selectedGenre == null
                FilterChip(
                    selected = isAllGenre,
                    onClick = { vm.selectGenre(null) },
                    label = { Text("Semua Genre", fontSize = 12.sp, fontWeight = if (isAllGenre) FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Sumi, selectedLabelColor = PaperIvory, containerColor = PaperIvory, labelColor = Sumi)
                )
                if (vm.genres.isEmpty()) {
                    repeat(6) {
                        Box(Modifier.width(64.dp).height(32.dp).clip(RoundedCornerShape(999.dp)).background(SpineMist))
                    }
                } else {
                    vm.genres.take(18).forEach { g ->
                        val active = vm.selectedGenre == g.value
                        FilterChip(
                            selected = active,
                            onClick = { vm.selectGenre(g.value) },
                            label = { Text(g.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Hanko, selectedLabelColor = PaperIvory, containerColor = PaperIvory, labelColor = Sumi)
                        )
                    }
                }
            }
            HorizontalDivider(color = Outline, thickness = 1.dp)
            // Content with PullToRefresh
            PullToRefreshBox(
                isRefreshing = vm.isRefreshing,
                onRefresh = { vm.refresh() },
                modifier = Modifier.fillMaxSize().background(PaperIvory)
            ) {
                when {
                    vm.loading && vm.items.isEmpty() -> {
                        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(5) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SpineMist).padding(12.dp),
                                ) {
                                    Box(Modifier.width(4.dp).height(84.dp).clip(RoundedCornerShape(999.dp)).background(GalleyGrey.copy(alpha = 0.3f)))
                                    Spacer(Modifier.width(12.dp))
                                    Box(Modifier.size(width = 64.dp, height = 84.dp).clip(RoundedCornerShape(8.dp)).background(SpineMist2))
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(Modifier.height(14.dp).fillMaxWidth(0.6f).clip(RoundedCornerShape(4.dp)).background(SpineMist2))
                                        Box(Modifier.height(10.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(4.dp)).background(SpineMist2))
                                        Box(Modifier.height(10.dp).fillMaxWidth(0.8f).clip(RoundedCornerShape(4.dp)).background(SpineMist2))
                                    }
                                }
                            }
                        }
                    }
                    vm.error != null && vm.items.isEmpty() -> {
                        Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(if (vm.error!!.contains("30/menit")) "Kena batas 30/menit" else "Gagal memuat", color = Sumi, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(vm.error ?: "", color = GalleyGrey, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Button(onClick = { vm.retry() }, colors = ButtonDefaults.buttonColors(containerColor = Hanko)) { Text("Coba Lagi", color = PaperIvory) }
                            OutlinedButton(onClick = { vm.selectGenre(null); vm.selectOrder(HomeOrder.Terbaru) }) { Text("Reset Filter") }
                            if (vm.error?.contains("30/menit") == true) {
                                Card(colors = CardDefaults.cardColors(containerColor = HankoBg)) {
                                    Text("API cuma 30 req/menit. Jangan spam tarik — tunggu 30 detik. Library tetap offline.", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = Sumi)
                                }
                            }
                            if (vm.selectedGenre != null) {
                                Text("Genre ${vm.selectedGenre} kosong — coba genre lain", fontSize = 11.sp, color = GalleyGrey)
                            }
                        }
                    }
                    else -> {
                        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                            // Trending strip preview (hanya saat Terbaru + tanpa genre)
                            if (vm.trendingPreview.isNotEmpty() && vm.selectedOrder == HomeOrder.Terbaru && vm.selectedGenre == null) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("🔥 TRENDING HARI INI", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = Sumi)
                                            TextButton(onClick = { vm.selectOrder(HomeOrder.Trending) }) { Text("Lihat semua", fontSize = 12.sp, color = Hanko) }
                                        }
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 8.dp)) {
                                            items(vm.trendingPreview, key = { it.slug }) { t ->
                                                com.zaaamzomic.ui.components.MangaSpineCardCompact(
                                                    title = t.title, thumbnail = t.thumbnail, chapterInfo = t.description, onClick = { onMangaClick(t.slug) }
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Outline.copy(alpha = 0.4f))
                                    }
                                }
                            }
                            // Header pill
                            item {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        when {
                                            vm.selectedGenre != null -> "GENRE • ${vm.selectedGenre!!.uppercase()}"
                                            else -> "${vm.selectedOrder.label.uppercase()} • ${vm.items.size} judul"
                                        },
                                        fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 1.sp, color = GalleyGrey, fontWeight = FontWeight.Bold
                                    )
                                    if (vm.selectedGenre != null) TextButton(onClick = { vm.selectGenre(null) }) { Text("× Hapus filter", fontSize = 11.sp, color = Hanko) }
                                }
                            }
                            itemsIndexed(vm.items, key = { idx, manga -> "${manga.slug}_${manga.title}_$idx" }) { _, manga ->
                                com.zaaamzomic.ui.components.MangaSpineCard(
                                    title = manga.title,
                                    thumbnail = manga.thumbnail,
                                    type = manga.type,
                                    genre = manga.genre,
                                    chapterInfo = manga.description?.takeIf { it.contains("Chapter") || it.contains("menit") || it.contains("jam") || it.contains("hari") },
                                    description = manga.description,
                                    publicationStatus = PublicationStatus.UNKNOWN,
                                    readingStatus = null,
                                    onClick = { onMangaClick(manga.slug) }
                                )
                            }
                            item {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    val footer = when {
                                        vm.selectedGenre != null -> "GET /comic/genre/${vm.selectedGenre} • ${vm.items.size} judul"
                                        vm.selectedOrder == HomeOrder.Terbaru -> "GET /comic/terbaru • ${vm.items.size} judul • Komiku"
                                        vm.selectedOrder == HomeOrder.Populer -> "GET /comic/populer • ${vm.items.size} judul • ranking"
                                        vm.selectedOrder == HomeOrder.Trending -> "GET /comic/trending • ${vm.items.size} judul • today"
                                        else -> "GET /comic/berwarna/1 • ${vm.items.size} judul • Berwarna"
                                    }
                                    Text(footer, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey, modifier = Modifier.background(SpineMist, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                            if (vm.items.isNotEmpty()) {
                                item { Spacer(Modifier.height(4.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
