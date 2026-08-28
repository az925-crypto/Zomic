package com.zaaamzomic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zaaamzomic.AppContainer
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.network.MangaSummaryDto
import com.zaaamzomic.ui.theme.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(private val container: AppContainer) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _results = MutableStateFlow<List<MangaSummaryDto>>(emptyList())
    val results: StateFlow<List<MangaSummaryDto>> = _results

    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    init {
        @OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        _query
            .debounce(450)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .flatMapLatest { q ->
                flow {
                    loading = true; error = null
                    try {
                        val res = container.sankaService.search(q)
                        _results.value = res.data
                        emit(res.data)
                    } catch (e: Exception) {
                        error = e.message
                        if (e.message?.contains("429") == true) error = "Kena batas 30/menit — coba lagi sebentar"
                        emit(emptyList<MangaSummaryDto>())
                    } finally { loading = false }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(v: String) { _query.value = v }
    fun clear() { _query.value = ""; _results.value = emptyList(); error = null }
    fun retry() {
        viewModelScope.launch {
            val q = _query.value
            if (q.length < 2) return@launch
            loading = true; error = null
            try {
                val res = container.sankaService.search(q)
                _results.value = res.data
            } catch (e: Exception) {
                error = e.message
                if (e.message?.contains("429") == true) error = "Kena batas 30/menit — coba lagi sebentar"
            } finally { loading = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(container: AppContainer, onMangaClick: (String) -> Unit) {
    val vm: SearchViewModel = viewModel(factory = viewModelFactory { initializer { SearchViewModel(container) } })
    val q by vm.query.collectAsState()
    val results by vm.results.collectAsState()
    val recent = listOf("one piece", "naruto", "solo leveling")

    Scaffold(containerColor = PaperIvory) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().background(PaperIvory).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = q,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari judul, cth: naruto", color = GalleyGrey) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Hanko,
                    unfocusedBorderColor = if (q.isEmpty()) Outline else Hanko,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Hanko
                ),
                trailingIcon = {
                    if (q.isNotEmpty()) TextButton(onClick = { vm.clear() }) { Text("✕", color = GalleyGrey) }
                }
            )
            Spacer(Modifier.height(16.dp))
            when {
                q.length < 2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Pencarian terakhir", fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, color = GalleyGrey)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                recent.forEach { r ->
                                    AssistChip(
                                        onClick = { vm.onQueryChange(r) },
                                        label = { Text(r, fontSize = 12.sp) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White, labelColor = Sumi)
                                    )
                                }
                            }
                        }
                        Column {
                            Text("Coba kata kunci", fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, color = GalleyGrey)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Jujutsu Kaisen", "Chainsaw Man", "Boruto").forEach { kw ->
                                    AssistChip(
                                        onClick = { vm.onQueryChange(kw.lowercase()) },
                                        label = { Text(kw, fontSize = 12.sp) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White, labelColor = Sumi)
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SpineMist).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(Modifier.size(120.dp, 80.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.6f)))
                            Spacer(Modifier.height(16.dp))
                            Text("Mulai cari", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Sumi)
                            Text("Ketik minimal 2 huruf. Pencarian debounce 450ms — patuh rate limit 30/menit.", fontSize = 12.sp, color = GalleyGrey, modifier = Modifier.padding(top = 6.dp))
                            Text("GET /comic/search?q=", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey, modifier = Modifier.padding(top = 8.dp).background(Color.White, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
                vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Hanko) }
                vm.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(vm.error ?: "Error", fontWeight = FontWeight.Bold, color = Sumi)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.retry() }, colors = ButtonDefaults.buttonColors(containerColor = Hanko)) { Text("Coba Lagi", color = Color.White) }
                    }
                }
                results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("Nggak ketemu “$q”", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Sumi)
                        Spacer(Modifier.height(6.dp))
                        Text("Coba “One Piece” atau “Solo Leveling”", fontSize = 12.sp, color = GalleyGrey)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { vm.onQueryChange("one piece") }) { Text("Coba “One Piece”", color = Sumi) }
                    }
                }
                else -> {
                    Column {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hasil untuk “$q”".uppercase(), fontSize = 11.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.Bold, color = GalleyGrey)
                            Text("${results.size} ditemukan", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey)
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(results, key = { idx, manga -> "${manga.slug}_${manga.title}_$idx" }) { _, manga ->
                                com.zaaamzomic.ui.components.MangaSpineCard(
                                    title = manga.title,
                                    thumbnail = manga.thumbnail,
                                    type = manga.type,
                                    genre = manga.genre,
                                    chapterInfo = null,
                                    description = manga.description,
                                    publicationStatus = PublicationStatus.UNKNOWN,
                                    readingStatus = null,
                                    onClick = { onMangaClick(manga.slug) }
                                )
                            }
                            item {
                                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("q=$q", fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.background(SpineMist, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp), color = Sumi)
                                    Text("total: ${results.size}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.background(Sumi, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
