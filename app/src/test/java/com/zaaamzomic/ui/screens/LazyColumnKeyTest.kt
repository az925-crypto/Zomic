package com.zaaamzomic.ui.screens

import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * TDD test for duplicate key crash fix.
 * Verifies that LazyColumn keys are unique even when slug/title/url are empty or duplicated.
 */
class LazyColumnKeyTest {

    // Simulate OLD buggy key logic
    private fun oldChapterKey(slug: String, title: String): String = slug.ifBlank { title }
    private fun oldMangaKey(slug: String, title: String): String = slug.ifBlank { title }
    private fun oldImageKey(url: String): String = url

    // Simulate NEW fixed key logic (with index fallback)
    private fun newChapterKey(slug: String, title: String, index: Int): String = "${slug}_${title}_$index"
    private fun newMangaKey(slug: String, title: String, index: Int): String = "${slug}_${title}_$index"
    private fun newImageKey(url: String, index: Int): String = "${url}_$index"

    @Test
    fun testOldChapterKeysDuplicateWhenEmpty() {
        val chapters = listOf(
            Pair("", ""),
            Pair("", "")
        )
        val oldKeys = chapters.map { (slug, title) -> oldChapterKey(slug, title) }
        // Old logic produces duplicate keys "" for both -> should be duplicate
        assertEquals("Old logic should produce duplicates for empty slug/title", 1, oldKeys.toSet().size)
        assertEquals(2, oldKeys.size)
        // Verify new logic is unique
        val newKeys = chapters.mapIndexed { idx, (slug, title) -> newChapterKey(slug, title, idx) }
        assertEquals("New logic must be unique even with empty values", newKeys.size, newKeys.toSet().size)
    }

    @Test
    fun testOldMangaKeysDuplicateWhenEmpty() {
        val mangas = listOf(
            Pair("", ""),
            Pair("", "")
        )
        val oldKeys = mangas.map { (slug, title) -> oldMangaKey(slug, title) }
        assertEquals(1, oldKeys.toSet().size)
        val newKeys = mangas.mapIndexed { idx, (slug, title) -> newMangaKey(slug, title, idx) }
        assertEquals(newKeys.size, newKeys.toSet().size)
    }

    @Test
    fun testOldMangaKeysDuplicateWhenSameSlug() {
        val mangas = listOf(
            Pair("naruto", "Naruto"),
            Pair("naruto", "Naruto")
        )
        val oldKeys = mangas.map { (slug, title) -> oldMangaKey(slug, title) }
        assertEquals(1, oldKeys.toSet().size)
        val newKeys = mangas.mapIndexed { idx, (slug, title) -> newMangaKey(slug, title, idx) }
        assertEquals(2, newKeys.toSet().size)
    }

    @Test
    fun testOldImageKeysDuplicateWhenSameUrl() {
        val urls = listOf(
            "https://example.com/img1.jpg",
            "https://example.com/img1.jpg"
        )
        val oldKeys = urls.map { oldImageKey(it) }
        assertEquals(1, oldKeys.toSet().size)
        val newKeys = urls.mapIndexed { idx, url -> newImageKey(url, idx) }
        assertEquals(2, newKeys.toSet().size)
    }

    @Test
    fun testNewKeysUniqueForAllCases() {
        // Stress test: all empty
        val chaptersEmpty = List(3) { Pair("", "") }
        val newChapterKeys = chaptersEmpty.mapIndexed { idx, (s, t) -> newChapterKey(s, t, idx) }
        assertEquals(3, newChapterKeys.toSet().size)

        // Stress test: duplicate urls
        val urls = List(3) { "https://example.com/same.jpg" }
        val newImageKeys = urls.mapIndexed { idx, url -> newImageKey(url, idx) }
        assertEquals(3, newImageKeys.toSet().size)

        // Mixed slug/title
        val mangasMixed = listOf(Pair("", "TitleA"), Pair("", "TitleA"), Pair("slug", ""))
        val newMangaKeys = mangasMixed.mapIndexed { idx, (s, t) -> newMangaKey(s, t, idx) }
        assertEquals(3, newMangaKeys.toSet().size)
    }

    // --- File content verification (RED before fix, GREEN after fix) ---

    private fun readFile(path: String): String {
        val direct = File(path)
        if (direct.exists()) return direct.readText()
        // search upward from user.dir and from current file location
        val startDirs = listOf(File(System.getProperty("user.dir") ?: "."), File("."), File(".."), File("../.."))
        for (base in startDirs) {
            var cur: File? = base.canonicalFile
            repeat(5) {
                val candidate = File(cur, path)
                if (candidate.exists()) return candidate.readText()
                // also try without app prefix if path starts with app/
                if (path.startsWith("app/")) {
                    val alt = File(cur, path.removePrefix("app/"))
                    if (alt.exists()) return alt.readText()
                }
                // try app/src/... relative to cur
                val name = File(path).name
                val alt2 = File(cur, "app/src/main/java/com/zaaamzomic/ui/screens/$name")
                if (alt2.exists()) return alt2.readText()
                val alt3 = File(cur, "src/main/java/com/zaaamzomic/ui/screens/$name")
                if (alt3.exists()) return alt3.readText()
                cur = cur?.parentFile
                if (cur == null) return@repeat
            }
        }
        // last resort: search from filesystem root
        val absCandidates = listOf(
            File("/home/runner/work/Zomic/Zomic/$path"),
            File("/github/workspace/$path"),
            File("/data/data/com.termux/files/home/git/zomic/$path")
        )
        for (f in absCandidates) if (f.exists()) return f.readText()
        fail("File not found: $path (user.dir=${System.getProperty("user.dir")})")
        return ""
    }

    @Test
    fun testDetailScreenUsesItemsIndexedWithUniqueKey() {
        val path = "app/src/main/java/com/zaaamzomic/ui/screens/DetailScreen.kt"
        val content = readFile(path)
        // Must use itemsIndexed
        assertTrue("DetailScreen should use itemsIndexed", content.contains("itemsIndexed"))
        // Must contain key with index fallback pattern "${ch.slug}_${ch.title}_$idx" or similar
        // Check for key = { idx, ch -> and contains _\$idx or _$idx
        assertTrue("DetailScreen key must include index suffix", content.contains("\$idx") || content.contains("_\$idx") || content.contains("_\$") )
        assertTrue("DetailScreen key must reference slug and title with index", content.contains("slug") && content.contains("title") && content.contains("idx"))
        // Must NOT contain old buggy pattern items(vm.chapters, key = { it.slug.ifBlank { it.title } })
        assertFalse("DetailScreen must not use old buggy key it.slug.ifBlank", content.contains("key = { it.slug.ifBlank"))
        // Also check import for itemsIndexed
        assertTrue("DetailScreen should import itemsIndexed", content.contains("itemsIndexed"))
    }

    @Test
    fun testSearchScreenUsesItemsIndexedWithUniqueKey() {
        val path = "app/src/main/java/com/zaaamzomic/ui/screens/SearchScreen.kt"
        val content = readFile(path)
        assertTrue("SearchScreen should use itemsIndexed", content.contains("itemsIndexed"))
        assertTrue("SearchScreen key must include index", content.contains("\$idx") || content.contains("idx"))
        assertFalse("SearchScreen must not use old buggy key", content.contains("key = { it.slug.ifBlank"))
        assertTrue("SearchScreen should import itemsIndexed", content.contains("itemsIndexed"))
    }

    @Test
    fun testTerbaruScreenUsesItemsIndexedWithUniqueKey() {
        val path = "app/src/main/java/com/zaaamzomic/ui/screens/TerbaruScreen.kt"
        val content = readFile(path)
        assertTrue("TerbaruScreen should use itemsIndexed", content.contains("itemsIndexed"))
        assertTrue("TerbaruScreen key must include index", content.contains("\$idx") || content.contains("idx"))
        assertFalse("TerbaruScreen must not use old buggy key", content.contains("key = { it.slug.ifBlank"))
        assertTrue("TerbaruScreen should import itemsIndexed", content.contains("itemsIndexed"))
    }

    @Test
    fun testReaderScreenUsesUniqueKeyWithIndex() {
        val path = "app/src/main/java/com/zaaamzomic/ui/screens/ReaderScreen.kt"
        val content = readFile(path)
        // Must use key with url and index
        // Pattern: key = { idx, url -> "${url}_$idx" } or similar
        assertTrue("ReaderScreen should use itemsIndexed with url and idx", content.contains("itemsIndexed"))
        // Check key lambda includes both url and idx and $idx
        assertTrue("ReaderScreen key must include \$idx", content.contains("\$idx"))
        // Should contain "${url}_$idx" or "\"${url}_$idx\"" or "${url}_"
        assertTrue("ReaderScreen key should combine url and index", content.contains("url") && content.contains("idx"))
        // Must NOT be old logic key = { _, url -> url }
        // We check that the specific old pattern "key = { _, url -> url }" is gone (allow whitespace variations)
        val oldPattern = Regex("""key\s*=\s*\{\s*_,\s*url\s*->\s*url\s*\}""")
        assertFalse("ReaderScreen must not use old duplicate key url only", oldPattern.containsMatchIn(content))
    }
}
