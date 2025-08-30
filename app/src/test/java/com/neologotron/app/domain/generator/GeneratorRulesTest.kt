package com.neologotron.app.domain.generator

import org.junit.Assert.*
import org.junit.Test

class GeneratorRulesTest {

    @Test
    fun elides_prefix_vowel_when_no_connector_and_root_starts_with_vowel() {
        // bio- + aero- + -logie, with no connector, should drop trailing 'o' in prefix
        val word = GeneratorRules.composeWord(prefixForm = "bio-", rootForm = "aero-", suffixForm = "-logie", connectorPref = "").word
        // Should not contain "bioa" sequence; should contain "bia" at the boundary
        assertFalse(word.contains("bioa"))
        assertTrue(word.contains("biaero"))
    }

    @Test
    fun inserts_liaison_vowel_once_when_connector_provided() {
        // Case 1: prefix without trailing 'o' should insert liaison 'o'
        val word1 = GeneratorRules.composeWord(prefixForm = "inter-", rootForm = "astro-", suffixForm = "-logie", connectorPref = "o").word
        assertTrue("Expected liaison 'o' between inter and astro", word1.contains("interoastro"))

        // Case 2: prefix ending with 'o' should NOT duplicate liaison
        val word2 = GeneratorRules.composeWord(prefixForm = "micro-", rootForm = "astro-", suffixForm = "-logie", connectorPref = "o").word
        assertFalse("Should not duplicate 'o' at boundary", word2.contains("microoastro"))
        assertTrue(word2.contains("microastro"))
    }

    @Test
    fun treats_h_as_vowel_for_elision() {
        val word = GeneratorRules.composeWord(prefixForm = "bio-", rootForm = "hémo-", suffixForm = "-logie", connectorPref = "").word
        assertTrue(word.startsWith("bih"))
        assertFalse(word.startsWith("bioh"))
    }

    @Test
    fun removes_duplicate_consonant_between_parts() {
        val word = GeneratorRules.composeWord(prefixForm = "trans-", rootForm = "son-", suffixForm = "-ique", connectorPref = "").word
        assertFalse(word.contains("transson"))
        assertTrue(word.contains("transon"))
    }

    @Test
    fun plausibility_penalizes_vowel_vowel_boundary() {
        val result = GeneratorRules.composeWord(prefixForm = "", rootForm = "aero-", suffixForm = "-algie", connectorPref = "")
        assertTrue("Score should be below 1.0 for vowel-vowel boundary", result.plausibility < 1.0)
    }

    @Test
    fun composeDefinition_uses_mode_specific_template() {
        val tpl = "tech:instrument qui {ACTION} {ROOT}|poetic:qui évoque {ROOT}"
        val tech = GeneratorRules.composeDefinition("lumière", "nom", tpl, mode = GeneratorRules.DefinitionMode.TECHNICAL)
        val poet = GeneratorRules.composeDefinition("lumière", "nom", tpl, mode = GeneratorRules.DefinitionMode.POETIC)
        assertEquals("instrument qui concerne lumière", tech)
        assertEquals("qui évoque lumière", poet)
    }

    @Test
    fun composeDefinition_default_adj_in_technical_mode_is_qualifie() {
        val def = GeneratorRules.composeDefinition("lumière", "adj", null, mode = GeneratorRules.DefinitionMode.TECHNICAL)
        assertEquals("qui qualifie lumière", def)
    }

    @Test
    fun composeDefinition_default_poetic_falls_back_to_evoque() {
        val def1 = GeneratorRules.composeDefinition("lumière", "nom", null, mode = GeneratorRules.DefinitionMode.POETIC)
        val def2 = GeneratorRules.composeDefinition("lumière", null, null, mode = GeneratorRules.DefinitionMode.POETIC)
        assertEquals("qui évoque lumière", def1)
        assertEquals("qui évoque lumière", def2)
    }

    @Test
    fun composeDefinition_action_for_verb_uses_agir_sur_when_template_requires_action() {
        val tpl = "tech:{ACTION} {ROOT}"
        val def = GeneratorRules.composeDefinition("lumière", "verbe", tpl, mode = GeneratorRules.DefinitionMode.TECHNICAL)
        assertEquals("agir sur lumière", def)
    }

    @Test
    fun composeDefinition_defaults_for_adverb() {
        val tech = GeneratorRules.composeDefinition("lumière", "adv", null, mode = GeneratorRules.DefinitionMode.TECHNICAL)
        val poet = GeneratorRules.composeDefinition("lumière", "adv", null, mode = GeneratorRules.DefinitionMode.POETIC)
        assertEquals("de manière liée à lumière", tech)
        assertEquals("d'une manière qui évoque lumière", poet)
    }

    @Test
    fun composeDefinition_action_and_result_nouns_map_actions() {
        val act = GeneratorRules.composeDefinition("lumière", "nom_action", "tech:{ACTION} {ROOT}", mode = GeneratorRules.DefinitionMode.TECHNICAL)
        val res = GeneratorRules.composeDefinition("lumière", "nom_resultat", "tech:{ACTION} {ROOT}", mode = GeneratorRules.DefinitionMode.TECHNICAL)
        assertEquals("action de lumière", act)
        assertEquals("résultat de lumière", res)
    }

    @Test
    fun composeDefinition_agent_noun_default_template() {
        val def = GeneratorRules.composeDefinition("lumière", "nom_agent", null, mode = GeneratorRules.DefinitionMode.TECHNICAL)
        assertEquals("agent lié à lumière", def)
    }

    @Test
    fun composeDefinition_composite_adj_nom_prefers_adj() {
        val def = GeneratorRules.composeDefinition("lumière", "adj/nom", null, mode = GeneratorRules.DefinitionMode.TECHNICAL)
        assertEquals("qui qualifie lumière", def)
    }
}
