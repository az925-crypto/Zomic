package com.zaaamzomic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.zaaamzomic.ui.theme.*
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
                val final = res.effectiveImages
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
        containerColor = Sumi,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Sumi, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((mangaSlug ?: "Manga").uppercase() + " — " + chapterSlug.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 0.8.sp, color = Color.White)
                        val total = vm.images.size
                        val displayPage = if (total == 0) 0 else (vm.pageIndex + 1).coerceIn(1, total)
                        Text("P. $displayPage / $total • tap tengah untuk overlay", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = GalleyGrey2)
                    }
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Kembali", color = Color.White, fontSize = 12.sp) } },
                actions = { TextButton(onClick = {}) { Text("Bookmark", color = Color.White, fontSize = 12.sp) } }
            )
        },
        bottomBar = {
            // scrub like mockup 64dp
            Column(
                modifier = Modifier.fillMaxWidth().background(Sumi).padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(999.dp)).background(SumiVariant)) {
                    val pct = if (vm.images.isEmpty()) 0f else (vm.pageIndex + 1).toFloat() / vm.images.size
                    Box(Modifier.fillMaxHeight().fillMaxWidth(pct.coerceIn(0f, 1f)).background(Hanko2))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {}) { Text("‹ Ch. prev", color = Hanko2, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Text("P. ${if (vm.images.isEmpty()) 0 else vm.pageIndex + 1} / ${vm.images.size} • progress auto-save 500ms", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = GalleyGrey2)
                    TextButton(onClick = {}) { Text("Ch. next ›", color = Hanko2, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Text("GET /comic/chapter/$chapterSlug • Coil disk 250MB • preload ±2 hal", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF5A6470), modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().background(Sumi)) {
            when {
                vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Hanko2) }
                vm.error != null -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(vm.error ?: "Error", color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = Hanko2)) { Text("Muat Ulang Halaman", color = Color.White) }
                }
                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize().background(Sumi), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(vm.images, key = { idx, url -> "${url}_$idx" }) { idx, url ->
                        var retryKey by remember(url) { mutableStateOf(0) }
                        var hasError by remember(url) { mutableStateOf(false) }
                        Column {
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SumiSurface).let { mod ->
                                    if (hasError) mod.background(SumiSurface) else mod
                                }
                            ) {
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
                                    Box(Modifier.fillMaxWidth().height(220.dp).background(SumiSurface), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                            Text("Gagal load halaman ${idx + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Koneksi lemah • retry per halaman", color = GalleyGrey2, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                            Spacer(Modifier.height(10.dp))
                                            Button(onClick = { hasError = false; retryKey++ }, colors = ButtonDefaults.buttonColors(containerColor = Hanko2), shape = RoundedCornerShape(999.dp)) { Text("Muat Ulang Halaman", color = Color.White, fontSize = 12.sp) }
                                        }
                                    }
                                }
                                if (!hasError && vm.images.isNotEmpty() && idx == 0) {
                                    // overlay for aspect ratio hint like mockup
                                    Box(Modifier.align(Alignment.Center)) { }
                                }
                            }
                            // gutter dashed like mockup
                            if (idx != vm.images.lastIndex) {
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(1.dp).background(Color.White.copy(alpha = 0.12f)))
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).height(1.dp).background(SumiVariant))
                            Text(" Page Gutter — lipatan buku (dashed) ", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = GalleyGrey2)
                            Box(Modifier.weight(1f).height(1.dp).background(SumiVariant))
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
