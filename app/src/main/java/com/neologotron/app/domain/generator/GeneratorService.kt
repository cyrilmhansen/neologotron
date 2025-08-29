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
    val decomposition: String
)

@Singleton
class GeneratorService @Inject constructor(
    private val lexemes: LexemeRepository,
    private val history: HistoryRepository
) {
    suspend fun generateRandom(tags: Set<String> = emptySet(), saveToHistory: Boolean = true): WordResult {
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

        val word = compose(p, r, s)
        val definition = composeDefinition(r, s)
        val decomposition = "${p.form} + ${r.form} + ${s.form}"

        if (saveToHistory) history.add(word, definition, decomposition, mode = "random")
        return WordResult(word, definition, decomposition)
    }

    private fun compose(p: PrefixEntity, r: RootEntity, s: SuffixEntity): String {
        return GeneratorRules.composeWord(p.form, r.form, s.form, r.connectorPref)
    }

    private fun composeDefinition(r: RootEntity, s: SuffixEntity): String {
        return GeneratorRules.composeDefinition(r.gloss, s.posOut, s.defTemplate)
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
