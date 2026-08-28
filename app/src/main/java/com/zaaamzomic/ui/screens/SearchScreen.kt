package com.zaaamzomic.ui.screens

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
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.network.MangaSummaryDto
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

    Scaffold { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = q,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari judul, cth: naruto") },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            if (q.length < 2) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ketik minimal 2 huruf")
                        Spacer(Modifier.height(4.dp))
                        Text("Pencarian debounce 450ms • patuh 30/menit", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (vm.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(vm.error ?: "Error")
                        Button(onClick = { vm.retry() }) { Text("Coba Lagi") }
                    }
                }
            } else if (results.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nggak ketemu “$q”")
                        Text("Coba “One Piece” atau “Solo Leveling”", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                }
            }
        }
    }
}
