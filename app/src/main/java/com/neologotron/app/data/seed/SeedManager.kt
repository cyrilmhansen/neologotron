package com.neologotron.app.data.seed

import android.content.Context
import android.util.Log
import com.neologotron.app.data.db.AppDatabase
import com.neologotron.app.data.entity.DbMetaEntity
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.BufferedReader
import java.io.InputStreamReader

@Singleton
class SeedManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun seedIfEmpty(db: AppDatabase) {
        val prefixDao = db.prefixDao()
        val rootDao = db.rootDao()
        val suffixDao = db.suffixDao()
        val metaDao = db.metaDao()

        var seeded = false
        if (prefixDao.count() == 0) {
            prefixDao.insertAll(readPrefixes())
            seeded = true
        }
        if (rootDao.count() == 0) {
            rootDao.insertAll(readRoots())
            seeded = true
        }
        if (suffixDao.count() == 0) {
            suffixDao.insertAll(readSuffixes())
            seeded = true
        }
        if (seeded && metaDao.get() == null) {
            metaDao.insert(DbMetaEntity(createdAtMillis = System.currentTimeMillis(), version = 1))
        }
    }

    suspend fun seedAll(db: AppDatabase) {
        db.prefixDao().insertAll(readPrefixes())
        db.rootDao().insertAll(readRoots())
        db.suffixDao().insertAll(readSuffixes())
        db.metaDao().insert(DbMetaEntity(createdAtMillis = System.currentTimeMillis(), version = 1))
    }

    private fun readPrefixes() = readCsv("seed/neologotron_prefixes.csv").mapNotNull { row ->
        try {
            PrefixEntity(
                id = row[0],
                form = row[1],
                altForms = row.getOrNull(2),
                gloss = row.getOrNull(3) ?: "",
                origin = row.getOrNull(4),
                connector = row.getOrNull(5),
                phonRules = row.getOrNull(6),
                tags = row.getOrNull(7),
                weight = row.getOrNull(8)?.toDoubleOrNull()
            )
        } catch (_: Exception) { null }
    }

    private fun readRoots() = readCsv("seed/neologotron_racines.csv").mapNotNull { row ->
        try {
            RootEntity(
                id = row[0],
                form = row[1],
                altForms = row.getOrNull(2),
                gloss = row.getOrNull(3) ?: "",
                origin = row.getOrNull(4),
                domain = row.getOrNull(5),
                connectorPref = row.getOrNull(6),
                examples = row.getOrNull(7),
                weight = row.getOrNull(8)?.toDoubleOrNull()
            )
        } catch (_: Exception) { null }
    }

    private fun readSuffixes() = readCsv("seed/neologotron_suffixes.csv").mapNotNull { row ->
        try {
            SuffixEntity(
                id = row[0],
                form = row[1],
                altForms = row.getOrNull(2),
                gloss = row.getOrNull(3) ?: "",
                origin = row.getOrNull(4),
                posOut = row.getOrNull(5),
                defTemplate = row.getOrNull(6),
                tags = row.getOrNull(7),
                weight = row.getOrNull(8)?.toDoubleOrNull()
            )
        } catch (_: Exception) { null }
    }

    private fun readCsv(assetPath: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        try {
            context.assets.open(assetPath).use { input ->
                BufferedReader(InputStreamReader(input)).use { br ->
                    var line = br.readLine()
                    var isHeader = true
                    while (line != null) {
                        if (!isHeader) parseCsvLine(line)?.let { result.add(it) }
                        isHeader = false
                        line = br.readLine()
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e("SeedManager", "Failed to read $assetPath", t)
        }
        return result
    }


}

// Exposed for unit testing and reuse. Parses a single CSV line into fields.
// Rules:
// - Commas inside quotes are preserved
// - Double quotes inside quoted fields are unescaped ("")
// - Blank lines return null
internal fun parseCsvLine(line: String): List<String>? {
    if (line.isBlank()) return null
    val fields = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when (c) {
            '"' -> {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"'); i++
                } else {
                    inQuotes = !inQuotes
                }
            }
            ',' -> {
                if (inQuotes) sb.append(c) else { fields.add(sb.toString()); sb.setLength(0) }
            }
            else -> sb.append(c)
        }
        i++
    }
    fields.add(sb.toString())
    return fields
}
