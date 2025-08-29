package com.neologotron.app.domain.generator

object GeneratorRules {
    fun composeWord(prefixForm: String, rootForm: String, suffixForm: String, connectorPref: String?): String {
        val pref = prefixForm.trim().trimEnd('-')
        val root = rootForm.trim().trimEnd('-')
        val suff = suffixForm.trim().trimStart('-')

        val connector = (connectorPref ?: "").trim()
        val needsElision = connector.isEmpty() && startsWithVowel(root) && endsWithVowel(pref)
        val prefixAdjusted = if (needsElision && pref.isNotEmpty()) pref.dropLast(1) else pref

        val liaison = if (connector.isNotEmpty()) connector else ""
        val mid = if (liaison.isNotEmpty() && !(pref.endsWith(liaison) || root.startsWith(liaison))) liaison else ""

        return (prefixAdjusted + mid + root + suff)
    }

    fun composeDefinition(rootGloss: String, suffixPosOut: String?, defTemplate: String?): String {
        val tpl = defTemplate ?: "relatif à {ROOT}"
        val action = when (suffixPosOut?.lowercase()) {
            "nom_agent" -> "qui concerne"
            "nom" -> "concerne"
            "adj" -> "qualifie"
            else -> "créer"
        }
        return tpl.replace("{ROOT}", rootGloss).replace("{ACTION}", action)
    }

    private fun startsWithVowel(s: String): Boolean = s.firstOrNull()?.lowercaseChar()?.let { it in vowels } ?: false
    private fun endsWithVowel(s: String): Boolean = s.lastOrNull()?.lowercaseChar()?.let { it in vowels } ?: false
    private val vowels = setOf('a','e','i','o','u','y','é','è','ê','ë','ï','î','ô','û','ù')
}

