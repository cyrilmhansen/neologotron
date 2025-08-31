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

        fun mapOne(p: String): CanonPos =
            when {
                // Adjective variants
                p.startsWith("adj") || p.startsWith("adjectif") || p == "adj." -> CanonPos.ADJECTIVE
                // Adverb variants
                p.startsWith("adv") || p.startsWith("adverbe") || p == "adv." -> CanonPos.ADVERB
                // Verb variants (FR/EN)
                p.startsWith("verb") || p.startsWith("verbe") || p == "v" || p.contains("transitif") || p.contains("intransitif") -> CanonPos.VERB
                // Agentive noun variants
                p.contains("agent") || p.contains("agentif") || p.contains("profession") -> CanonPos.AGENT_NOUN
                // Action/result nouns
                p.contains("action") || p.contains("processus") -> CanonPos.ACTION_NOUN
                p.contains("result") || p.contains("résultat") || p.contains("resultat") || p.contains("produit") -> CanonPos.RESULT_NOUN
                // Noun variants
                p.startsWith("nom") || p.startsWith("subst") || p.startsWith("nominal") || p == "n" -> CanonPos.NOUN
                else -> CanonPos.UNKNOWN
            }
        val mapped = parts.map { mapOne(it) }
        // Priority: AGENT > ACTION > RESULT > VERB > ADJ > NOUN > UNKNOWN
        val priority =
            listOf(
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
        mode: DefinitionMode = DefinitionMode.TECHNICAL,
    ): String {
        val templates = defTemplate?.let { parseTemplates(it) }
        val tagsLc = tags?.lowercase().orEmpty()
        val isPoetic = tagsLc.contains("poétique") || tagsLc.contains("poetique")
        val isTech = tagsLc.contains("tech") || tagsLc.contains("technique") || tagsLc.contains("science")
        val canonPos = normalizePos(suffixPosOut)
        val deRoot = buildDe(rootGloss)

        // Fallback templates when none provided
        val defaultTpl =
            when (mode) {
                DefinitionMode.TECHNICAL ->
                    when (canonPos) {
                        CanonPos.ADJECTIVE -> "qui qualifie {ROOT}"
                        CanonPos.ADVERB -> "d'une manière liée à {ROOT}"
                        CanonPos.VERB -> "{ACTION} {ROOT}"
                        CanonPos.ACTION_NOUN -> "action {DE_ROOT}"
                        CanonPos.RESULT_NOUN -> "résultat {DE_ROOT}"
                        CanonPos.AGENT_NOUN -> "qui {ACTION} {ROOT}"
                        CanonPos.NOUN, CanonPos.UNKNOWN ->
                            when {
                                tagsLc.contains("instrument") || tagsLc.contains("outil") ->
                                    // For instruments, prefer a verb phrase even for noun POS
                                    "instrument qui agit sur {ROOT}"
                                // Domain-specific phrasing
                                tagsLc.contains("inflammation") ->
                                    "inflammation {DE_ROOT}"
                                tagsLc.contains("pathologie") || tagsLc.contains("maladie") || tagsLc.contains("patho") ->
                                    "pathologie {DE_ROOT}"
                                tagsLc.contains("médecine") || tagsLc.contains("medecine") || tagsLc.contains("médical") || tagsLc.contains("medical") ->
                                    "terme médical lié à {ROOT}"
                                tagsLc.contains("biologie") || tagsLc.contains("biologique") || tagsLc.contains("bio") ->
                                    "phénomène biologique lié à {ROOT}"
                                tagsLc.contains("informatique") || tagsLc.contains("logiciel") || tagsLc.contains("numérique") || tagsLc.contains("numerique") ->
                                    "processus informatique {DE_ROOT}"
                                tagsLc.contains("technologie") || tagsLc.contains("technologique") ->
                                    "procédé technique {DE_ROOT}"
                                tagsLc.contains("science") || tagsLc.contains("discipline") || tagsLc.contains("étude") || tagsLc.contains("etude") || tagsLc.contains("logie") ->
                                    "étude {DE_ROOT}"
                                tagsLc.contains("société") || tagsLc.contains("societe") || tagsLc.contains("social") || tagsLc.contains("sociologie") ->
                                    "phénomène social lié à {ROOT}"
                                tagsLc.contains("lieu") || tagsLc.contains("toponyme") ->
                                    "lieu lié à {ROOT}"
                                isTech -> "{ACTION} {DE_ROOT}"
                                else -> "relatif à {ROOT}"
                            }
                    }
                DefinitionMode.POETIC -> {
                    // Tag-driven poetic defaults
                    when {
                        tagsLc.contains("inflammation") || tagsLc.contains("pathologie") || tagsLc.contains("maladie") || tagsLc.contains("médecine") || tagsLc.contains("medecine") || tagsLc.contains("médical") || tagsLc.contains("medical") ->
                            "la douleur des tissus {DE_ROOT}"
                        tagsLc.contains("biologie") || tagsLc.contains("biologique") || tagsLc.contains("bio") ->
                            "la vie qui {ACTION} {ROOT}"
                        tagsLc.contains("informatique") || tagsLc.contains("logiciel") || tagsLc.contains("numérique") || tagsLc.contains("numerique") || tagsLc.contains("technologie") || tagsLc.contains("technologique") ->
                            "la machine qui {ACTION} {ROOT}"
                        tagsLc.contains("société") || tagsLc.contains("societe") || tagsLc.contains("social") || tagsLc.contains("sociologie") ->
                            "le mouvement social lié à {ROOT}"
                        tagsLc.contains("lieu") || tagsLc.contains("toponyme") ->
                            "le lieu lié à {ROOT}"
                        else ->
                            when (canonPos) {
                                CanonPos.ADVERB -> "d'une manière qui évoque {ROOT}"
                                else -> "qui évoque {ROOT}"
                            }
                    }
                }
            }
        val tpl = templates?.get(mode) ?: defTemplate ?: defaultTpl
        val action =
            when (canonPos) {
                CanonPos.AGENT_NOUN -> "agit sur"
                CanonPos.NOUN -> "concerne"
                CanonPos.ADJECTIVE -> "qualifie"
                CanonPos.VERB -> "agir sur"
                CanonPos.ACTION_NOUN -> "action de"
                CanonPos.RESULT_NOUN -> "résultat de"
                CanonPos.ADVERB -> "se rapporte à"
                CanonPos.UNKNOWN -> "concerne"
            }
        return tpl
            .replace("{DE_ROOT}", deRoot)
            .replace("{ROOT}", rootGloss)
            .replace("{ACTION}", action)
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

    private fun plausibility(
        prefix: String,
        root: String,
        suffix: String,
        connector: String,
    ): Double {
        fun penalty(
            a: Char?,
            b: Char?,
        ): Double {
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

    private val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y', 'é', 'è', 'ê', 'ë', 'ï', 'î', 'ô', 'û', 'ù')

    private fun buildDe(root: String): String {
        val first = root.firstOrNull()?.lowercaseChar() ?: return "de $root"
        val elide = first in vowels || startsWithSilentH(root)
        return if (elide) "d'" + root else "de $root"
    }
}
