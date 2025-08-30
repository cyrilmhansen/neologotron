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
}

