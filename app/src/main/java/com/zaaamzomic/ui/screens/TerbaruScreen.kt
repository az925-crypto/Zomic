package com.zaaamzomic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
import com.zaaamzomic.data.network.MangaSummaryDto
import com.zaaamzomic.ui.theme.*
import kotlinx.coroutines.launch

class TerbaruViewModel(private val container: AppContainer) : ViewModel() {
    var items by mutableStateOf<List<MangaSummaryDto>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun load() {
        if (loading) return
        viewModelScope.launch {
            loading = true; error = null
            try {
                val res = container.sankaService.getTerbaru()
                val mapped = when {
                    res.comics.isNotEmpty() -> res.comics.map { it.toSummary() }
                    res.data != null && res.data.isNotEmpty() -> res.data
                    else -> emptyList()
                }
                items = mapped
                if (mapped.isEmpty()) error = "Tidak ada data terbaru"
            } catch (e: Exception) {
                val msg = e.message ?: "Gagal memuat"
                error = if (msg.contains("429")) "Kena batas 30/menit — coba lagi sebentar" else msg
            } finally { loading = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerbaruScreen(
    container: AppContainer,
    onMangaClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val vm: TerbaruViewModel = viewModel(factory = viewModelFactory { initializer { TerbaruViewModel(container) } })
    LaunchedEffect(Unit) { vm.load() }
    var selectedType by remember { mutableStateOf("Semua") }

    Scaffold(
        containerColor = PaperIvory,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperIvory, titleContentColor = Sumi),
                title = {
                    Column {
                        Text("ZOMIC", fontFamily = FontFamily.Serif, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.2.sp, color = Sumi)
                        Text("TERBARU • KOMIKU DEFAULT", fontFamily = FontFamily.Monospace, fontSize = 8.sp, letterSpacing = 1.4.sp, color = GalleyGrey, fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(PaperIvory)) {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = Sumi)
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(PaperIvory)) {
            // Filter row like mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperIvory)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Semua", "Manga", "Manhwa", "Manhua").forEach { label ->
                    val active = selectedType == label
                    FilterChip(
                        selected = active,
                        onClick = { selectedType = label },
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Hanko,
                            selectedLabelColor = PaperIvory,
                            containerColor = PaperIvory,
                            labelColor = Sumi
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (active) Hanko else Outline,
                            selectedBorderColor = Hanko,
                            enabled = true,
                            selected = active
                        )
                    )
                }
                Box(Modifier.width(1.dp).height(16.dp).background(Outline))
                AssistChip(
                    onClick = {},
                    label = { Text("Filter", fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = PaperIvory, labelColor = Sumi),
                    border = AssistChipDefaults.assistChipBorder(borderColor = Outline)
                )
            }
            HorizontalDivider(color = Outline, thickness = 1.dp)
            Box(Modifier.fillMaxSize().background(PaperIvory)) {
                when {
                    vm.loading && vm.items.isEmpty() -> {
                        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(4) {
                                // shimmer card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SpineMist)
                                        .padding(12.dp),
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
                    vm.error != null && vm.items.isEmpty() -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(vm.error ?: "Error", color = Sumi, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = Hanko)) { Text("Coba Lagi", color = PaperIvory) }
                        Spacer(Modifier.height(8.dp))
                        Text("Fallback: Library tetap bisa dibuka offline", style = MaterialTheme.typography.bodySmall, color = GalleyGrey)
                        if (vm.error?.contains("30/menit") == true) {
                            Spacer(Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = HankoBg)) {
                                Text("429 — Rate limit 30/menit. Tunggu sebentar lalu coba lagi.", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = Sumi)
                            }
                        }
                    }
                    else -> {
                        val filtered = if (selectedType == "Semua") vm.items else vm.items.filter { it.type?.equals(selectedType, ignoreCase = true) == true }
                        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(filtered, key = { idx, manga -> "${manga.slug}_${manga.title}_$idx" }) { _, manga ->
                                com.zaaamzomic.ui.components.MangaSpineCard(
                                    title = manga.title,
                                    thumbnail = manga.thumbnail,
                                    type = manga.type,
                                    genre = manga.genre,
                                    chapterInfo = manga.description?.takeIf { it.contains("Chapter") || it.contains("jam") },
                                    description = manga.description,
                                    publicationStatus = PublicationStatus.UNKNOWN,
                                    readingStatus = null,
                                    onClick = { onMangaClick(manga.slug) }
                                )
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                            item {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    Text("GET /comic/terbaru • ${filtered.size} judul • Komiku default", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey, modifier = Modifier.background(SpineMist, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
