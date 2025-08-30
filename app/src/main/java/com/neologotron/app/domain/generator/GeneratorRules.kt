package com.neologotron.app.domain.generator

object GeneratorRules {

    enum class DefinitionMode { TECHNICAL, POETIC }

    data class WordBuild(val word: String, val plausibility: Double)

    private enum class CanonPos { ADJECTIVE, ADVERB, VERB, NOUN, AGENT_NOUN, ACTION_NOUN, RESULT_NOUN, UNKNOWN }

    private fun normalizePos(pos: String?): CanonPos {
        val raw = pos?.lowercase()?.trim() ?: return CanonPos.UNKNOWN
        // Handle composite values like "adj/nom" or lists
        val parts = raw.split('/', ',', ';', ' ').filter { it.isNotBlank() }
        if (parts.isEmpty()) return CanonPos.UNKNOWN
        fun mapOne(p: String): CanonPos = when {
            p.startsWith("adj") -> CanonPos.ADJECTIVE
            p.startsWith("adv") -> CanonPos.ADVERB
            p.startsWith("verb") || p.startsWith("verbe") || p == "v" -> CanonPos.VERB
            p.contains("agent") -> CanonPos.AGENT_NOUN
            p.contains("action") -> CanonPos.ACTION_NOUN
            p.contains("result") || p.contains("résultat") || p.contains("resultat") -> CanonPos.RESULT_NOUN
            p.startsWith("nom") -> CanonPos.NOUN
            else -> CanonPos.UNKNOWN
        }
        val mapped = parts.map { mapOne(it) }
        // Priority: AGENT > ACTION > RESULT > VERB > ADJ > NOUN > UNKNOWN
        val priority = listOf(
            CanonPos.AGENT_NOUN,
            CanonPos.ACTION_NOUN,
            CanonPos.RESULT_NOUN,
            CanonPos.VERB,
            CanonPos.ADJECTIVE,
            CanonPos.NOUN,
            CanonPos.ADVERB,
            CanonPos.UNKNOWN,
        )
        return priority.firstOrNull { mapped.contains(it) } ?: CanonPos.UNKNOWN
    }

    fun composeWord(
        prefixForm: String,
        rootForm: String,
        suffixForm: String,
        connectorPref: String?,
        useFilters: Boolean = true,
    ): WordBuild {
        val pref = prefixForm.trim().trimEnd('-')
        var root = rootForm.trim().trimEnd('-')
        var suff = suffixForm.trim().trimStart('-')

        if (!useFilters) {
            val word = pref + root + suff
            return WordBuild(word, 1.0)
        }

        val connector = (connectorPref ?: "").trim()
        val needsElision = connector.isEmpty() && (startsWithVowel(root) || startsWithSilentH(root)) && endsWithVowel(pref)
        val prefixAdjusted = if (needsElision && pref.isNotEmpty()) pref.dropLast(1) else pref

        if (prefixAdjusted.isNotEmpty() && root.isNotEmpty() &&
            prefixAdjusted.last().lowercaseChar() == root.first().lowercaseChar() &&
            !startsWithVowel(root)
        ) {
            root = root.drop(1)
        }
        if (root.isNotEmpty() && suff.isNotEmpty() &&
            root.last().lowercaseChar() == suff.first().lowercaseChar() &&
            !startsWithVowel(suff)
        ) {
            suff = suff.drop(1)
        }

        val liaison = if (connector.isNotEmpty()) connector else ""
        val mid = if (liaison.isNotEmpty() && !(pref.endsWith(liaison) || root.startsWith(liaison))) liaison else ""

        val word = prefixAdjusted + mid + root + suff
        val score = plausibility(prefixAdjusted, root, suff, mid)
        return WordBuild(word, score)
    }

    fun composeDefinition(
        rootGloss: String,
        suffixPosOut: String?,
        defTemplate: String?,
        tags: String? = null,
        mode: DefinitionMode = DefinitionMode.TECHNICAL
    ): String {
        val templates = defTemplate?.let { parseTemplates(it) }
        val isPoetic = tags?.lowercase()?.contains("poétique") == true || tags?.lowercase()?.contains("poetique") == true
        val isTech = tags?.lowercase()?.contains("tech") == true
        val canonPos = normalizePos(suffixPosOut)
        // Fallback templates when none provided
        val defaultTpl = when (mode) {
            DefinitionMode.TECHNICAL -> when (canonPos) {
                CanonPos.ADJECTIVE -> "qui qualifie {ROOT}"
                CanonPos.ADVERB -> "de manière liée à {ROOT}"
                CanonPos.VERB -> "{ACTION} {ROOT}"
                CanonPos.ACTION_NOUN -> "{ACTION} {ROOT}"
                CanonPos.RESULT_NOUN -> "{ACTION} {ROOT}"
                CanonPos.AGENT_NOUN -> "agent lié à {ROOT}"
                CanonPos.NOUN, CanonPos.UNKNOWN -> if (isTech) "{ACTION} de {ROOT}" else "relatif à {ROOT}"
            }
            DefinitionMode.POETIC -> when (canonPos) {
                CanonPos.ADVERB -> "d'une manière qui évoque {ROOT}"
                else -> "qui évoque {ROOT}"
            }
        }
        val tpl = templates?.get(mode) ?: defTemplate ?: defaultTpl
        val action = when (canonPos) {
            CanonPos.AGENT_NOUN -> "qui concerne"
            CanonPos.NOUN -> "concerne"
            CanonPos.ADJECTIVE -> "qualifie"
            CanonPos.VERB -> "agir sur"
            CanonPos.ACTION_NOUN -> "action de"
            CanonPos.RESULT_NOUN -> "résultat de"
            CanonPos.ADVERB -> "se rapporte à"
            CanonPos.UNKNOWN -> "créer"
        }
        return tpl.replace("{ROOT}", rootGloss).replace("{ACTION}", action)
    }

    private fun parseTemplates(raw: String): Map<DefinitionMode, String> {
        val map = mutableMapOf<DefinitionMode, String>()
        raw.split('|').forEach { part ->
            val seg = part.split(":", limit = 2)
            if (seg.size == 2) {
                val key = seg[0].trim().lowercase()
                val value = seg[1].trim()
                when (key) {
                    "tech", "technical", "technique" -> map[DefinitionMode.TECHNICAL] = value
                    "poet", "poetic", "poetique", "poétique" -> map[DefinitionMode.POETIC] = value
                }
            }
        }
        return map
    }

    private fun plausibility(prefix: String, root: String, suffix: String, connector: String): Double {
        fun penalty(a: Char?, b: Char?): Double {
            if (a == null || b == null) return 0.0
            val av = a.lowercaseChar() in vowels
            val bv = b.lowercaseChar() in vowels
            return when {
                av && bv -> 0.3
                !av && !bv -> 0.1
                else -> 0.0
            }
        }
        var score = 1.0
        score -= penalty(prefix.lastOrNull(), root.firstOrNull())
        val boundarySecond = if (connector.isNotEmpty()) connector.lastOrNull() else root.lastOrNull()
        score -= penalty(boundarySecond, suffix.firstOrNull())
        return score.coerceIn(0.0, 1.0)
    }

    private fun startsWithVowel(s: String): Boolean {
        val first = s.firstOrNull()?.lowercaseChar() ?: return false
        return first in vowels
    }

    private fun startsWithSilentH(s: String): Boolean {
        return s.startsWith("h", ignoreCase = true) && s.getOrNull(1)?.lowercaseChar()?.let { it in vowels } == true
    }

    private fun endsWithVowel(s: String): Boolean = s.lastOrNull()?.lowercaseChar()?.let { it in vowels } ?: false
    private val vowels = setOf('a','e','i','o','u','y','é','è','ê','ë','ï','î','ô','û','ù')
}
