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
import androidx.compose.ui.platform.LocalContext
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
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.zaaamzomic.AppContainer
import com.zaaamzomic.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow

class ReaderViewModel(private val container: AppContainer, private val mangaSlug: String?, private val chapterSlug: String) : ViewModel() {
    private val ctx = container.context
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

    fun preload(from: Int, count: Int = 4) {
        val end = (from + count).coerceAtMost(images.size - 1)
        for (i in from..end) {
            if (i < 0 || i >= images.size) continue
            val req = ImageRequest.Builder(ctx)
                .data(images[i])
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            container.imageLoader.enqueue(req)
        }
    }
}

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
            vm.preload(idx)
        }
    }

    val total = vm.images.size
    val displayPage = if (total == 0) 0 else (vm.pageIndex + 1).coerceIn(1, total)
    val pct = if (total == 0) 0f else (vm.pageIndex + 1).toFloat() / total

    Box(Modifier.fillMaxSize().background(Sumi)) {
        when {
            vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Hanko2) }
            vm.error != null -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(vm.error ?: "Error", color = Color.White)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.load() }, colors = ButtonDefaults.buttonColors(containerColor = Hanko2)) { Text("Muat Ulang Halaman", color = Color.White) }
            }
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().background(Sumi),
                contentPadding = PaddingValues(top = 44.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(vm.images, key = { idx, url -> "${url}_$idx" }) { idx, url ->
                    var retryKey by remember(url) { mutableStateOf(0) }
                    var hasError by remember(url) { mutableStateOf(false) }
                    Box(Modifier.fillMaxWidth().background(SumiSurface)) {
                        key(retryKey) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = "Hal ${idx + 1}",
                                modifier = Modifier.fillMaxWidth(),
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
                    }
                }
            }
        }
        // Top overlay slim 44dp
        Row(
            modifier = Modifier.fillMaxWidth().background(Sumi.copy(alpha = 0.92f)).padding(horizontal = 4.dp).height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("‹", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f)) {
                Text((mangaSlug ?: "Manga").uppercase(), fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 0.8.sp, color = Color.White, maxLines = 1)
                Text("Ch. ${chapterSlug.substringAfterLast('-')} • P. $displayPage/$total", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = GalleyGrey2, maxLines = 1)
            }
            TextButton(onClick = {}, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Bookmark", color = Hanko2, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }
        // Bottom scrub compact 56dp
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Sumi.copy(alpha = 0.92f)).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(999.dp)).background(SumiVariant)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(pct.coerceIn(0f, 1f)).background(Hanko2))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {}, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("‹ Prev", color = Hanko2, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                Text("P. $displayPage / $total • auto-save 500ms", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = GalleyGrey2)
                TextButton(onClick = {}, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("Next ›", color = Hanko2, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
