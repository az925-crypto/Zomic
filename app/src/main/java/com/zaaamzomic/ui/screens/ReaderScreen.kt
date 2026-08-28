package com.zaaamzomic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.zaaamzomic.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow

class ReaderViewModel(private val container: AppContainer, private val mangaSlug: String?, private val chapterSlug: String) : ViewModel() {
    var images by mutableStateOf<List<String>>(emptyList())
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var pageIndex by mutableStateOf(0)
    private var saveJob: Job? = null

    fun load() {
        viewModelScope.launch {
            loading = true; error = null
            try {
                val res = container.sankaService.getChapter(chapterSlug)
                val dto = res.data
                val imgs = dto?.images?.ifEmpty { dto.imageList ?: dto.chapterImages ?: emptyList() } ?: emptyList()
                val final = if (imgs.isEmpty() && dto?.image != null) listOf(dto.image) else imgs
                // sanitize: only https
                val sanitized = final.filter { it.startsWith("https://") }
                images = sanitized
                if (sanitized.isEmpty() && final.isNotEmpty()) error = "Gambar tidak valid (bukan https)"
                else if (sanitized.isEmpty()) error = "Gambar tidak tersedia"
            } catch (e: Exception) {
                error = e.message ?: "Gagal load chapter"
            } finally { loading = false }
        }
    }

    fun onPageChanged(index: Int) {
        val clamped = index.coerceIn(0, (images.size - 1).coerceAtLeast(0))
        pageIndex = clamped
        if (images.isEmpty()) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            if (mangaSlug != null) {
                container.libraryRepository.saveProgress(mangaSlug, chapterSlug, chapterSlug, clamped, images.size)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    container: AppContainer,
    mangaSlug: String?,
    chapterSlug: String,
    onBack: () -> Unit,
) {
    val vm: ReaderViewModel = viewModel(key = chapterSlug, factory = viewModelFactory { initializer { ReaderViewModel(container, mangaSlug, chapterSlug) } })
    LaunchedEffect(chapterSlug) { vm.load() }
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collectLatest { idx ->
            vm.onPageChanged(idx)
        }
    }

    Scaffold(
        topBar = {
            val total = vm.images.size
            val displayPage = if (total == 0) 0 else (vm.pageIndex + 1).coerceIn(1, total)
            TopAppBar(
                title = { Text("$chapterSlug • P. $displayPage / $total") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Kembali") } }
            )
        },
        bottomBar = {
            BottomAppBar {
                Text("Progress disimpan otomatis", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when {
                vm.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                vm.error != null -> Column(Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(vm.error ?: "Error")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.load() }) { Text("Muat Ulang Halaman") }
                }
                else -> LazyColumn(state = listState, contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(vm.images, key = { idx, url -> "${url}_$idx" }) { idx, url ->
                        var retryKey by remember(url) { mutableStateOf(0) }
                        var hasError by remember(url) { mutableStateOf(false) }
                        Card {
                            key(retryKey) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data(url)
                                        .crossfade(true)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "Hal ${idx + 1}",
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                                    contentScale = ContentScale.FillWidth,
                                    onError = { hasError = true },
                                    onSuccess = { hasError = false },
                                )
                            }
                            if (hasError) {
                                TextButton(onClick = { hasError = false; retryKey++ }) { Text("Muat Ulang Halaman ${idx + 1}") }
                            }
                        }
                    }
                }
            }
        }
    }
}
