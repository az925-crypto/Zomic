package com.zaaamzomic.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zaaamzomic.AppContainer
import com.zaaamzomic.ui.screens.DetailScreen
import com.zaaamzomic.ui.screens.LibraryScreen
import com.zaaamzomic.ui.screens.ReaderScreen
import com.zaaamzomic.ui.screens.SearchScreen
import com.zaaamzomic.ui.screens.TerbaruScreen

sealed class Route(val path: String) {
    object Terbaru : Route("terbaru")
    object Search : Route("search")
    object Library : Route("library")
    object Detail : Route("detail/{slug}") {
        fun create(slug: String) = "detail/${android.net.Uri.encode(slug)}"
    }
    object Reader : Route("chapter/{slug}?mangaSlug={mangaSlug}&page={page}") {
        fun create(chapterSlug: String, mangaSlug: String? = null, page: Int = 0): String {
            val base = "chapter/${android.net.Uri.encode(chapterSlug)}"
            val q = mutableListOf<String>()
            if (mangaSlug != null) q.add("mangaSlug=${android.net.Uri.encode(mangaSlug)}")
            q.add("page=$page")
            return "$base?${q.joinToString("&")}"
        }
    }
}

@Composable
fun ZomicNav(container: AppContainer) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    val showBottom = current == Route.Terbaru.path || current == Route.Search.path || current == Route.Library.path

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    NavigationBarItem(
                        selected = current == Route.Terbaru.path,
                        onClick = { nav.navigate(Route.Terbaru.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Terbaru") }
                    )
                    NavigationBarItem(
                        selected = current == Route.Search.path,
                        onClick = { nav.navigate(Route.Search.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Cari") }
                    )
                    NavigationBarItem(
                        selected = current == Route.Library.path,
                        onClick = { nav.navigate(Route.Library.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Book, contentDescription = null) },
                        label = { Text("Library") }
                    )
                }
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = Route.Terbaru.path,
            modifier = Modifier.padding(pad)
        ) {
            composable(Route.Terbaru.path) {
                TerbaruScreen(
                    container = container,
                    onMangaClick = { slug -> nav.navigate(Route.Detail.create(slug)) },
                    onSearchClick = { nav.navigate(Route.Search.path) }
                )
            }
            composable(Route.Search.path) {
                SearchScreen(container = container, onMangaClick = { slug -> nav.navigate(Route.Detail.create(slug)) })
            }
            composable(Route.Library.path) {
                LibraryScreen(
                    container = container,
                    onMangaClick = { slug -> nav.navigate(Route.Detail.create(slug)) },
                    onContinueReading = { mangaSlug, chapterSlug, page ->
                        nav.navigate(Route.Reader.create(chapterSlug, mangaSlug, page))
                    }
                )
            }
            composable(
                Route.Detail.path,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { entry ->
                val slug = entry.arguments?.getString("slug") ?: return@composable
                DetailScreen(
                    container = container,
                    slug = slug,
                    onChapterClick = { chapterSlug -> nav.navigate(Route.Reader.create(chapterSlug, slug)) },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(
                Route.Reader.path,
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                    navArgument("mangaSlug") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("page") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { entry ->
                val slug = entry.arguments?.getString("slug") ?: return@composable
                val mangaSlug = entry.arguments?.getString("mangaSlug")
                ReaderScreen(container = container, mangaSlug = mangaSlug, chapterSlug = slug, onBack = { nav.popBackStack() })
            }
        }
    }
}
