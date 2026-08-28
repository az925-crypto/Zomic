package com.zaaamzomic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zaaamzomic.AppContainer
import com.zaaamzomic.data.db.PublicationStatus
import com.zaaamzomic.data.network.MangaSummaryDto
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
                items = res.data
            } catch (e: Exception) {
                error = e.message ?: "Gagal memuat"
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
    // Use LaunchedEffect to load
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZOMIC", style = MaterialTheme.typography.displaySmall) },
                actions = {
                    IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, contentDescription = "Cari") }
                }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when {
                vm.loading && vm.items.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                vm.error != null && vm.items.isEmpty() -> Column(Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(vm.error ?: "Error")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.load() }) { Text("Coba Lagi") }
                    Text("Fallback: Library tetap bisa dibuka offline", style = MaterialTheme.typography.bodySmall)
                }
                else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(vm.items, key = { idx, manga -> "${manga.slug}_${manga.title}_$idx" }) { _, manga ->
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
