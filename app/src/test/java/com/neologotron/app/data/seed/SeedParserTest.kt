package com.neologotron.app.data.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeedParserTest {
    @Test
    fun parse_simple_csv_line() {
        val line = "a,b,c"
        val fields = parseCsvLine(line)
        assertEquals(listOf("a", "b", "c"), fields)
    }

    @Test
    fun parse_handles_commas_inside_quotes() {
        val line = "a,\"b,b2\",c"
        val fields = parseCsvLine(line)
        assertEquals(listOf("a", "b,b2", "c"), fields)
    }

    @Test
    fun parse_unescapes_double_quotes_in_quoted_field() {
        // Second field is: "b""c" which represents b"c
        val line = "\"a\",\"b\"\"c\",d"
        val fields = parseCsvLine(line)
        assertEquals(listOf("a", "b\"c", "d"), fields)
    }

    @Test
    fun parse_blank_line_returns_null() {
        assertNull(parseCsvLine(""))
        assertNull(parseCsvLine("   "))
    }
}
