package com.neologotron.app.domain.generator

import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.data.repo.HistoryRepository
import com.neologotron.app.data.repo.LexemeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class WordResult(
    val word: String,
    val definition: String,
    val decomposition: String,
    val plausibility: Double,
    val prefixForm: String? = null,
    val rootForm: String? = null,
    val suffixForm: String? = null,
    val rootGloss: String? = null,
    val rootConnectorPref: String? = null,
    val suffixPosOut: String? = null,
    val suffixDefTemplate: String? = null,
    val suffixTags: String? = null,
)

@Singleton
class GeneratorService @Inject constructor(
    private val lexemes: LexemeRepository,
    private val history: HistoryRepository
) {
    suspend fun generateRandom(
        tags: Set<String> = emptySet(),
        saveToHistory: Boolean = true,
        mode: GeneratorRules.DefinitionMode = GeneratorRules.DefinitionMode.TECHNICAL,
        useFilters: Boolean = true,
    ): WordResult {
        val chosenTag = tags.shuffled().firstOrNull().orEmpty()
        val prefixes = if (chosenTag.isNotBlank()) lexemes.listPrefixesByTag(chosenTag) else lexemes.listPrefixesByTag("")
        val roots = if (chosenTag.isNotBlank()) lexemes.listRootsByTag(chosenTag) else lexemes.listRootsByTag("")
        val suffixes = if (chosenTag.isNotBlank()) lexemes.listSuffixesByTag(chosenTag) else lexemes.listSuffixesByTag("")

        val p = weightedRandom(prefixes) { it.weight }
            ?: throw IllegalStateException("No prefixes available")
        val r = weightedRandom(roots) { it.weight }
            ?: throw IllegalStateException("No roots available")
        val s = weightedRandom(suffixes) { it.weight }
            ?: throw IllegalStateException("No suffixes available")

        val composed = compose(p, r, s, useFilters)
        val word = composed.word
        val definition = composeDefinition(r, s, mode)
        val decomposition = "${p.form} + ${r.form} + ${s.form}"

        if (saveToHistory) history.add(word, definition, decomposition, mode = "random")
        return WordResult(
            word = word,
            definition = definition,
            decomposition = decomposition,
            plausibility = composed.plausibility,
            prefixForm = p.form,
            rootForm = r.form,
            suffixForm = s.form,
            rootGloss = r.gloss,
            rootConnectorPref = r.connectorPref,
            suffixPosOut = s.posOut,
            suffixDefTemplate = s.defTemplate,
            suffixTags = s.tags,
        )
    }

    private fun compose(
        p: PrefixEntity,
        r: RootEntity,
        s: SuffixEntity,
        useFilters: Boolean
    ): GeneratorRules.WordBuild {
        return GeneratorRules.composeWord(p.form, r.form, s.form, r.connectorPref, useFilters)
    }

    private fun composeDefinition(r: RootEntity, s: SuffixEntity, mode: GeneratorRules.DefinitionMode): String {
        return GeneratorRules.composeDefinition(r.gloss, s.posOut, s.defTemplate, s.tags, mode)
    }

    private fun <T> List<T>.randomOrNull(): T? = if (isEmpty()) null else this[Random.nextInt(size)]

    private fun <T> weightedRandom(items: List<T>, weightOf: (T) -> Double?): T? {
        if (items.isEmpty()) return null
        var total = 0.0
        val weights = items.map { (weightOf(it) ?: 1.0).coerceAtLeast(0.0) }.also { list ->
            total = list.sum().takeIf { it > 0 } ?: items.size.toDouble()
        }
        var r = Random.nextDouble(total)
        for (i in items.indices) {
            val w = weights[i].let { if (total == items.size.toDouble()) 1.0 else it }
            if (r < w) return items[i]
            r -= w
        }
        return items.last()
    }
}
