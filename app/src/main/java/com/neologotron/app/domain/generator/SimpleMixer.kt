package com.neologotron.app.domain.generator

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleMixer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        data class LexItem(val word: String, val tags: Set<String>, val register: String)

        private val lex: List<LexItem> by lazy { loadLexicon("lex/simple_lexicon.csv") }
        private val suf: List<String> by lazy { loadSuffixes("lex/simple_suffixes.csv") }

        private fun loadLexicon(path: String): List<LexItem> =
            try {
                context.assets.open(path).bufferedReader().useLines { lines ->
                    lines.drop(1).mapNotNull { line ->
                        val parts = line.split(',')
                        if (parts.isEmpty()) {
                            null
                        } else {
                            val w = parts.getOrNull(0)?.trim()?.ifBlank { null } ?: return@mapNotNull null
                            val tags = parts.getOrNull(1)?.split(';', ',')?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                            val reg = parts.getOrNull(2)?.trim().orEmpty()
                            LexItem(w, tags, reg)
                        }
                    }.toList()
                }
            } catch (_: Exception) {
                emptyList()
            }

        private fun loadSuffixes(path: String): List<String> =
            try {
                context.assets.open(path).bufferedReader().useLines { lines ->
                    lines.drop(1).mapNotNull { it.split(',').getOrNull(0)?.trim()?.ifBlank { null } }.toList()
                }
            } catch (_: Exception) {
                emptyList()
            }

        fun generate(tags: Set<String> = emptySet()): WordResult {
            val pool = if (tags.isEmpty()) lex else lex.filter { it.tags.intersect(tags).isNotEmpty() }.ifEmpty { lex }
            val a = pool.random()
            var b = pool.random()
            var tries = 0
            while ((b.word.equals(a.word, true) || b.word.length < 3) && tries++ < 10) b = pool.random()
            val word = blend(a.word, b.word)
            val def = "${a.word} + ${b.word}"
            val decomp = "${a.word} + ${b.word}"
            return WordResult(word = word, definition = def, decomposition = decomp, plausibility = 1.0)
        }

        private fun blend(
            a: String,
            b: String,
        ): String {
            val aa = a.trim().replace(" ", "")
            val bb = b.trim().replace(" ", "")
            val candidates = mutableListOf<String>()
            overlapMerge(aa, bb)?.let { candidates.add(it) }
            overlapMerge(bb, aa)?.let { candidates.add(it) }
            candidates.add(headTail(aa, bb))
            candidates.add(headTail(bb, aa))
            candidates.add(linkWithVowel(aa, bb))
            candidates.add(linkWithVowel(bb, aa))
            // Optional suffix play (random pick)
            suf.shuffled().firstOrNull()?.let { sfx ->
                if (aa.length in 4..9) candidates.add(aa + sfx)
                if (bb.length in 4..9) candidates.add(bb + sfx)
            }
            // Hyphen readable option
            candidates.add("$aa-$bb")
            var best =
                candidates.map { it to scoreCandidate(it, aa, bb) }.maxByOrNull { it.second }?.first
                    ?: headTail(aa, bb)
            // Optionally append a suffix to the best candidate (triple composant)
            suf.shuffled().firstOrNull()?.let { sfx ->
                val trial = normalize(best + sfx)
                val coreLen = trial.replace("-", "").length
                if (coreLen in 6..16 && isPronounceable(trial)) {
                    best = trial
                }
            }
            val cleaned = normalize(best)
            return if (cleaned.replace("-", "").length in 6..16) cleaned else cleaned.take(16)
        }

        private fun overlapMerge(
            a: String,
            b: String,
        ): String? {
            val al = a.lowercase()
            val bl = b.lowercase()
            for (k in minOf(a.length, b.length).downTo(3)) {
                if (al.takeLast(k) == bl.take(k)) {
                    return a + b.substring(k)
                }
            }
            return null
        }

        private fun headTail(
            a: String,
            b: String,
        ): String {
            val headMin = 4
            val tailMin = 4
            val head = a.take(maxOf(headMin, (a.length * 0.4).toInt().coerceAtMost(6)))
            val tail = b.takeLast(maxOf(tailMin, (b.length * 0.4).toInt().coerceAtMost(6)))
            val raw = head + tail
            return if (isPronounceable(raw)) raw else linkWithVowel(head, tail)
        }

        private fun linkWithVowel(
            left: String,
            right: String,
        ): String {
            if (left.isEmpty() || right.isEmpty()) return left + right
            val vowels = "aeiouyàâäéèêëîïôöùûü"
            val lc = left.last().lowercaseChar()
            val rf = right.first().lowercaseChar()
            return if (lc !in vowels && rf !in vowels) left + "o" + right else left + right
        }

        private fun normalize(s: String): String {
            val sb = StringBuilder()
            var prev1: Char? = null
            var prev2: Char? = null
            for (c in s) {
                if (prev1 != null && prev2 != null && prev1 == c.lowercaseChar() && prev2 == c.lowercaseChar()) continue
                sb.append(c)
                prev2 = prev1
                prev1 = c.lowercaseChar()
            }
            return sb.toString()
        }

        private fun scoreCandidate(
            c: String,
            a: String,
            b: String,
        ): Int {
            var score = 0
            val s = c.lowercase()
            if (isPronounceable(s)) score += 5 else score -= 3
            val la = lcsLen(s, a.lowercase())
            val lb = lcsLen(s, b.lowercase())
            if (la >= 4) score += la
            if (lb >= 4) score += lb
            if (s.contains('-')) score -= 2
            val len = s.replace("-", "").length
            score -= kotlin.math.abs(len - 9)
            return score
        }

        private fun isPronounceable(s: String): Boolean {
            val vowels = "aeiouyàâäéèêëîïôöùûü"
            var consRun = 0
            var hasVowel = false
            for (ch in s) {
                if (!ch.isLetter()) continue
                if (ch.lowercaseChar() in vowels) {
                    consRun = 0
                    hasVowel = true
                } else {
                    consRun++
                }
                if (consRun >= 4) return false
            }
            if (!hasVowel) return false
            if (s.contains('q') && !s.contains("qu")) return false
            return true
        }

        private fun lcsLen(
            x: String,
            y: String,
        ): Int {
            val m = x.length
            val n = y.length
            var best = 0
            val dp = Array(m + 1) { IntArray(n + 1) }
            for (i in 1..m) {
                for (j in 1..n) {
                    if (x[i - 1] == y[j - 1]) {
                        dp[i][j] = dp[i - 1][j - 1] + 1
                        if (dp[i][j] > best) best = dp[i][j]
                    }
                }
            }
            return best
        }
    }
