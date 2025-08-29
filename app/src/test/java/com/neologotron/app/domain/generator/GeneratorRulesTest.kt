package com.neologotron.app.domain.generator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratorRulesTest {

    @Test
    fun elides_prefix_vowel_when_no_connector_and_root_starts_with_vowel() {
        // bio- + aero- + -logie, with no connector, should drop trailing 'o' in prefix
        val word = GeneratorRules.composeWord(prefixForm = "bio-", rootForm = "aero-", suffixForm = "-logie", connectorPref = "")
        // Should not contain "bioa" sequence; should contain "bia" at the boundary
        assertFalse(word.contains("bioa"))
        assertTrue(word.contains("biaero"))
    }

    @Test
    fun inserts_liaison_vowel_once_when_connector_provided() {
        // Case 1: prefix without trailing 'o' should insert liaison 'o'
        val word1 = GeneratorRules.composeWord(prefixForm = "inter-", rootForm = "astro-", suffixForm = "-logie", connectorPref = "o")
        assertTrue("Expected liaison 'o' between inter and astro", word1.contains("interoastro"))

        // Case 2: prefix ending with 'o' should NOT duplicate liaison
        val word2 = GeneratorRules.composeWord(prefixForm = "micro-", rootForm = "astro-", suffixForm = "-logie", connectorPref = "o")
        assertFalse("Should not duplicate 'o' at boundary", word2.contains("microoastro"))
        assertTrue(word2.contains("microastro"))
    }
}

