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

class GeneratorServiceIntegrationTest {
    private class OneItemPrefixDao(private val item: PrefixEntity) : PrefixDao {
        override suspend fun insertAll(items: List<PrefixEntity>) {}

        override suspend fun count(): Int = 1

        override suspend fun getAll(): List<PrefixEntity> = listOf(item)

        override suspend fun findByTag(tag: String): List<PrefixEntity> = listOf(item)

        override suspend fun searchByForm(query: String): List<PrefixEntity> = listOf(item)

        override suspend fun clear() {}
    }

    private class OneItemRootDao(private val item: RootEntity) : RootDao {
        override suspend fun insertAll(items: List<RootEntity>) {}

        override suspend fun count(): Int = 1

        override suspend fun getAll(): List<RootEntity> = listOf(item)

        override suspend fun findByDomain(tag: String): List<RootEntity> = listOf(item)

        override suspend fun searchByForm(query: String): List<RootEntity> = listOf(item)

        override suspend fun clear() {}
    }

    private class OneItemSuffixDao(private val item: SuffixEntity) : SuffixDao {
        override suspend fun insertAll(items: List<SuffixEntity>) {}

        override suspend fun count(): Int = 1

        override suspend fun getAll(): List<SuffixEntity> = listOf(item)

        override suspend fun findByTag(tag: String): List<SuffixEntity> = listOf(item)

        override suspend fun searchByForm(query: String): List<SuffixEntity> = listOf(item)

        override suspend fun clear() {}
    }

    private object NoopHistoryDao : HistoryDao {
        override suspend fun insert(item: HistoryEntity) {}

        override suspend fun recent(limit: Int): List<HistoryEntity> = emptyList()

        override suspend fun latestByWord(word: String): HistoryEntity? = null

        override suspend fun clear() {}

        override suspend fun deleteById(id: Long) {}

        override suspend fun getById(id: Long): HistoryEntity? = null
    }

    @Test
    fun generate_respects_filters_and_templates() =
        runBlocking {
            val p =
                PrefixEntity(id = "p", form = "bio-", altForms = null, gloss = "vie", origin = null, connector = null, phonRules = null, tags = "", weight = 1.0)
            val r =
                RootEntity(id = "r", form = "aero-", altForms = null, gloss = "air", origin = null, domain = "", connectorPref = null, examples = null, weight = 1.0)
            val s =
                SuffixEntity(id = "s", form = "-logie", altForms = null, gloss = "étude", origin = null, posOut = "nom", defTemplate = "tech:étude de {ROOT}|poetic:qui évoque {ROOT}", tags = "", weight = 1.0)

            val repo = LexemeRepository(OneItemPrefixDao(p), OneItemRootDao(r), OneItemSuffixDao(s))
            val history = HistoryRepository(NoopHistoryDao)
            val service = GeneratorService(repo, history)

            val resTech =
                service.generateRandom(
                    tags = emptySet(),
                    saveToHistory = false,
                    mode = GeneratorRules.DefinitionMode.TECHNICAL,
                    useFilters = true,
                )
            // Elision: bio- + aero- -> bi + aero
            assertTrue(resTech.word.startsWith("biaero"))
            assertEquals("étude de air", resTech.definition)
            assertEquals("bio- + aero- + -logie", resTech.decomposition)

            val resPoetic =
                service.generateRandom(
                    tags = emptySet(),
                    saveToHistory = false,
                    mode = GeneratorRules.DefinitionMode.POETIC,
                    useFilters = true,
                )
            assertTrue(resPoetic.word.startsWith("biaero"))
            assertEquals("qui évoque air", resPoetic.definition)
        }

    @Test
    fun filters_toggle_affects_duplicate_consonant_boundary() =
        runBlocking {
            val p =
                PrefixEntity(id = "p", form = "trans-", altForms = null, gloss = "à travers", origin = null, connector = null, phonRules = null, tags = "", weight = 1.0)
            val r =
                RootEntity(id = "r", form = "son-", altForms = null, gloss = "son", origin = null, domain = "", connectorPref = null, examples = null, weight = 1.0)
            val s =
                SuffixEntity(id = "s", form = "-ique", altForms = null, gloss = "adj.", origin = null, posOut = "adj", defTemplate = null, tags = "", weight = 1.0)

            val repo = LexemeRepository(OneItemPrefixDao(p), OneItemRootDao(r), OneItemSuffixDao(s))
            val history = HistoryRepository(NoopHistoryDao)
            val service = GeneratorService(repo, history)

            val withFilters = service.generateRandom(saveToHistory = false, useFilters = true)
            val noFilters = service.generateRandom(saveToHistory = false, useFilters = false)

            assertTrue("Filtered version should collapse duplicate consonant at boundary", withFilters.word.contains("transon"))
            assertTrue("Unfiltered version should preserve duplicate consonant", noFilters.word.contains("transson"))
        }
}
