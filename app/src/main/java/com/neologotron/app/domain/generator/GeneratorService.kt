package com.neologotron.app.domain.generator

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.data.repo.HistoryRepository
import com.neologotron.app.data.repo.LexemeRepository

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
class GeneratorService
    @Inject
    constructor(
        private val lexemes: LexemeRepository,
        private val history: HistoryRepository,
        private val simple: SimpleMixer,
    ) {
        suspend fun generateRandom(
            tags: Set<String> = emptySet(),
            saveToHistory: Boolean = true,
            mode: GeneratorRules.DefinitionMode = GeneratorRules.DefinitionMode.TECHNICAL,
            useFilters: Boolean = true,
            weightingIntensity: Double = 1.0,
        ): WordResult {
            val t0 = System.nanoTime()
            val selected = tags.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
            // Fetch complete sets to allow multi-tag weighting; repository orders by base weight already
            val prefixes = lexemes.listPrefixesByTag("")
            val roots = lexemes.listRootsByTag("")
            val suffixes = lexemes.listSuffixesByTag("")

            val p =
                weightedRandom(prefixes) { pe ->
                    effectiveWeight(base = pe.weight, rawTags = pe.tags, selected = selected, intensity = weightingIntensity)
                } ?: throw IllegalStateException("No prefixes available")

            val r =
                weightedRandom(roots) { re ->
                    // For roots, we treat `domain` as tag-like field
                    effectiveWeight(base = re.weight, rawTags = re.domain, selected = selected, intensity = weightingIntensity)
                } ?: throw IllegalStateException("No roots available")

            val s =
                weightedRandom(suffixes) { se ->
                    effectiveWeight(base = se.weight, rawTags = se.tags, selected = selected, intensity = weightingIntensity)
                } ?: throw IllegalStateException("No suffixes available")

            val composed = compose(p, r, s, useFilters)
            val word = composed.word
            val definition = composeDefinition(r, s, mode)
            val decomposition = "${p.form} + ${r.form} + ${s.form}"

            val tCompose = System.nanoTime()
            if (saveToHistory) {
                history.add(
                    word = word,
                    definition = definition,
                    decomposition = decomposition,
                    mode = "random",
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
            val t1 = System.nanoTime()
            val composeMs = (tCompose - t0) / 1_000_000.0
            val totalMs = (t1 - t0) / 1_000_000.0
            if (DEBUG_LOG) {
                val msg = String.format("compose=%.1fms total=%.1fms tags=%d", composeMs, totalMs, selected.size)
                if (totalMs > 150.0) {
                    println(
                        "[WARN] $TAG: Generation latency >150ms: $msg",
                    )
                } else {
                    println("[DEBUG] $TAG: Generation latency: $msg")
                }
            }
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

        suspend fun generateSimple(
            tags: Set<String> = emptySet(),
            saveToHistory: Boolean = true,
            mode: GeneratorRules.DefinitionMode = GeneratorRules.DefinitionMode.TECHNICAL,
        ): WordResult {
            val r = simple.generate(tags)
            if (saveToHistory) {
                history.add(
                    word = r.word,
                    definition = r.definition,
                    decomposition = r.decomposition,
                    mode = "simple",
                )
            }
            return r
        }

        companion object {
            private const val TAG = "GeneratorService"
            private const val DEBUG_LOG = false
        }

        private fun effectiveWeight(
            base: Double?,
            rawTags: String?,
            selected: Set<String>,
            intensity: Double,
        ): Double {
            val baseW = (base ?: 1.0).coerceAtLeast(0.0)
            if (selected.isEmpty()) return baseW
            val itemTags =
                rawTags.orEmpty()
                    .split(',')
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .toSet()
            val matchCount = itemTags.intersect(selected).size
            // If at least one item in the collection matches, unmatched should be heavily deprioritized.
            // We return 0.0 for no matches and scale by (1 + matchCount * intensity) otherwise.
            return if (matchCount == 0) 0.0 else baseW * (1.0 + matchCount * intensity.coerceAtLeast(0.0))
        }

        private fun compose(
            p: PrefixEntity,
            r: RootEntity,
            s: SuffixEntity,
            useFilters: Boolean,
        ): GeneratorRules.WordBuild {
            return GeneratorRules.composeWord(p.form, r.form, s.form, r.connectorPref, useFilters)
        }

        private fun composeDefinition(
            r: RootEntity,
            s: SuffixEntity,
            mode: GeneratorRules.DefinitionMode,
        ): String {
            val root = (r.gloss.takeUnless { it.isBlank() } ?: r.form)
            val def = GeneratorRules.composeDefinition(root, s.posOut, s.defTemplate, s.tags, mode).trim()
            return if (def.isNotEmpty()) {
                def
            } else {
                val fallback = "relatif à $root".trim()
                Log.w(TAG, "Empty definition composed; using fallback | root='$root' posOut='${s.posOut}' tags='${s.tags}'")
                fallback
            }
        }

        private fun <T> List<T>.randomOrNull(): T? = if (isEmpty()) null else this[Random.nextInt(size)]

        private fun <T> weightedRandom(
            items: List<T>,
            weightOf: (T) -> Double?,
        ): T? {
            if (items.isEmpty()) return null
            var total = 0.0
            val weights =
                items.map { (weightOf(it) ?: 1.0).coerceAtLeast(0.0) }.also { list ->
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
