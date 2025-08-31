package com.neologotron.app.domain.generator

import kotlinx.coroutines.runBlocking
import com.neologotron.app.data.dao.HistoryDao
import com.neologotron.app.data.dao.PrefixDao
import com.neologotron.app.data.dao.RootDao
import com.neologotron.app.data.dao.SuffixDao
import com.neologotron.app.data.entity.HistoryEntity
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.data.repo.HistoryRepository
import com.neologotron.app.data.repo.LexemeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratorServiceWeightingTest {
    private class InMemoryPrefixDao(private val data: List<PrefixEntity>) : PrefixDao {
        override suspend fun insertAll(items: List<PrefixEntity>) {}

        override suspend fun count(): Int = data.size

        override suspend fun getAll(): List<PrefixEntity> = data

        override suspend fun findByTag(tag: String): List<PrefixEntity> = data.filter { (it.tags ?: "").contains(tag, ignoreCase = true) }

        override suspend fun searchByForm(query: String): List<PrefixEntity> = data.filter { it.form.contains(query, ignoreCase = true) }

        override suspend fun clear() {}
    }

    private class InMemoryRootDao(private val data: List<RootEntity>) : RootDao {
        override suspend fun insertAll(items: List<RootEntity>) {}

        override suspend fun count(): Int = data.size

        override suspend fun getAll(): List<RootEntity> = data

        override suspend fun findByDomain(tag: String): List<RootEntity> =
            data.filter {
                (it.domain ?: "").contains(tag, ignoreCase = true) || tag.isBlank()
            }

        override suspend fun searchByForm(query: String): List<RootEntity> = data.filter { it.form.contains(query, ignoreCase = true) }

        override suspend fun clear() {}
    }

    private class InMemorySuffixDao(private val data: List<SuffixEntity>) : SuffixDao {
        override suspend fun insertAll(items: List<SuffixEntity>) {}

        override suspend fun count(): Int = data.size

        override suspend fun getAll(): List<SuffixEntity> = data

        override suspend fun findByTag(tag: String): List<SuffixEntity> = data.filter { (it.tags ?: "").contains(tag, ignoreCase = true) }

        override suspend fun searchByForm(query: String): List<SuffixEntity> = data.filter { it.form.contains(query, ignoreCase = true) }

        override suspend fun clear() {}
    }

    // Minimal HistoryDao to satisfy HistoryRepository; we don't use it because saveToHistory=false in tests.
    private object NoopHistoryDao : HistoryDao {
        override suspend fun insert(item: HistoryEntity) {}

        override suspend fun recent(limit: Int): List<HistoryEntity> = emptyList()

        override suspend fun latestByWord(word: String): HistoryEntity? = null

        override suspend fun clear() {}

        override suspend fun deleteById(id: Long) {}

        override suspend fun getById(id: Long): HistoryEntity? = null
    }

    @Test
    fun multiTag_intersection_is_favored_over_single_tag_for_prefixes() =
        runBlocking {
            val prefixes =
                listOf(
                    PrefixEntity(id = "pI", form = "I-", altForms = null, gloss = "both", origin = null, connector = null, phonRules = null, tags = "alpha,beta", weight = 1.0),
                    PrefixEntity(id = "pS", form = "S-", altForms = null, gloss = "alpha", origin = null, connector = null, phonRules = null, tags = "alpha", weight = 1.0),
                    PrefixEntity(id = "pU", form = "U-", altForms = null, gloss = "none", origin = null, connector = null, phonRules = null, tags = "", weight = 1.0),
                )
            val roots =
                listOf(
                    RootEntity(id = "r", form = "root-", altForms = null, gloss = "g", origin = null, domain = "alpha,beta", connectorPref = null, examples = null, weight = 1.0),
                )
            val suffixes =
                listOf(
                    SuffixEntity(id = "s", form = "-x", altForms = null, gloss = "g", origin = null, posOut = "nom", defTemplate = null, tags = "", weight = 1.0),
                )
            val repo =
                LexemeRepository(
                    InMemoryPrefixDao(prefixes),
                    InMemoryRootDao(roots),
                    InMemorySuffixDao(suffixes),
                )
            val history = HistoryRepository(NoopHistoryDao)
            val service = GeneratorService(repo, history)

            val selected = setOf("alpha", "beta")
            val counts = mutableMapOf<String, Int>()
            repeat(600) {
                val w = service.generateRandom(tags = selected, saveToHistory = false, useFilters = false)
                val pf = w.prefixForm ?: "?"
                counts[pf] = (counts[pf] ?: 0) + 1
            }
            val iCount = counts["I-"] ?: 0
            val sCount = counts["S-"] ?: 0
            val uCount = counts["U-"] ?: 0

            // Intersection should beat single-tag by a meaningful margin,
            // and unmatched should never be chosen when matches exist.
            assertTrue("Intersection count should be greater than single-tag count", iCount > sCount)
            assertEquals("Unmatched prefix should not be selected when matches exist", 0, uCount)
        }

    @Test
    fun single_tag_filters_out_unmatched() =
        runBlocking {
            val prefixes =
                listOf(
                    PrefixEntity(id = "pI", form = "I-", altForms = null, gloss = "both", origin = null, connector = null, phonRules = null, tags = "alpha,beta", weight = 1.0),
                    PrefixEntity(id = "pS", form = "S-", altForms = null, gloss = "alpha", origin = null, connector = null, phonRules = null, tags = "alpha", weight = 1.0),
                    PrefixEntity(id = "pU", form = "U-", altForms = null, gloss = "none", origin = null, connector = null, phonRules = null, tags = "", weight = 1.0),
                )
            val roots =
                listOf(
                    RootEntity(id = "r", form = "root-", altForms = null, gloss = "g", origin = null, domain = "alpha", connectorPref = null, examples = null, weight = 1.0),
                )
            val suffixes =
                listOf(
                    SuffixEntity(id = "s", form = "-x", altForms = null, gloss = "g", origin = null, posOut = "nom", defTemplate = null, tags = "", weight = 1.0),
                )
            val repo =
                LexemeRepository(
                    InMemoryPrefixDao(prefixes),
                    InMemoryRootDao(roots),
                    InMemorySuffixDao(suffixes),
                )
            val history = HistoryRepository(NoopHistoryDao)
            val service = GeneratorService(repo, history)

            val counts = mutableMapOf<String, Int>()
            repeat(400) {
                val w = service.generateRandom(tags = setOf("alpha"), saveToHistory = false, useFilters = false)
                val pf = w.prefixForm ?: "?"
                counts[pf] = (counts[pf] ?: 0) + 1
            }
            val uCount = counts["U-"] ?: 0
            assertEquals("Unmatched prefix should not be selected when matches exist", 0, uCount)
        }

    @Test
    fun empty_selection_uses_base_weights() =
        runBlocking {
            val prefixes =
                listOf(
                    PrefixEntity(id = "p1", form = "A-", altForms = null, gloss = "", origin = null, connector = null, phonRules = null, tags = "", weight = 1.0),
                    PrefixEntity(id = "p2", form = "B-", altForms = null, gloss = "", origin = null, connector = null, phonRules = null, tags = "alpha", weight = 1.0),
                    PrefixEntity(id = "p3", form = "C-", altForms = null, gloss = "", origin = null, connector = null, phonRules = null, tags = "beta", weight = 1.0),
                )
            val roots =
                listOf(
                    RootEntity(id = "r", form = "root-", altForms = null, gloss = "g", origin = null, domain = "", connectorPref = null, examples = null, weight = 1.0),
                )
            val suffixes =
                listOf(
                    SuffixEntity(id = "s", form = "-x", altForms = null, gloss = "g", origin = null, posOut = "nom", defTemplate = null, tags = "", weight = 1.0),
                )
            val repo =
                LexemeRepository(
                    InMemoryPrefixDao(prefixes),
                    InMemoryRootDao(roots),
                    InMemorySuffixDao(suffixes),
                )
            val history = HistoryRepository(NoopHistoryDao)
            val service = GeneratorService(repo, history)

            val counts = mutableMapOf<String, Int>()
            repeat(300) {
                val w = service.generateRandom(tags = emptySet(), saveToHistory = false, useFilters = false)
                val pf = w.prefixForm ?: "?"
                counts[pf] = (counts[pf] ?: 0) + 1
            }
            // All three should appear at least once under uniform base weights
            assertTrue((counts["A-"] ?: 0) > 0)
            assertTrue((counts["B-"] ?: 0) > 0)
            assertTrue((counts["C-"] ?: 0) > 0)
        }
}
