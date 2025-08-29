package com.neologotron.app.data.repo

import com.neologotron.app.data.dao.PrefixDao
import com.neologotron.app.data.dao.RootDao
import com.neologotron.app.data.dao.SuffixDao
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LexemeRepository @Inject constructor(
    private val prefixDao: PrefixDao,
    private val rootDao: RootDao,
    private val suffixDao: SuffixDao
) {
    suspend fun getDistinctTags(): List<String> = withContext(Dispatchers.IO) {
        val tagSets = mutableSetOf<String>()
        prefixDao.getAll().forEach { it.tags?.let { t -> tagSets.addAll(splitTags(t)) } }
        rootDao.getAll().forEach { it.domain?.let { d -> tagSets.addAll(splitTags(d)) } }
        suffixDao.getAll().forEach { it.tags?.let { t -> tagSets.addAll(splitTags(t)) } }
        tagSets.filter { it.isNotBlank() }.sorted()
    }

    suspend fun listPrefixesByTag(tag: String): List<PrefixEntity> = withContext(Dispatchers.IO) {
        if (tag.isBlank()) prefixDao.getAll() else prefixDao.findByTag(tag)
    }

    suspend fun listRootsByTag(tag: String): List<RootEntity> = withContext(Dispatchers.IO) {
        // Using domain as tag-like column for roots
        if (tag.isBlank()) rootDao.getAll() else rootDao.findByDomain(tag)
    }

    suspend fun listSuffixesByTag(tag: String): List<SuffixEntity> = withContext(Dispatchers.IO) {
        if (tag.isBlank()) suffixDao.getAll() else suffixDao.findByTag(tag)
    }

    suspend fun searchPrefixes(query: String): List<PrefixEntity> = withContext(Dispatchers.IO) {
        prefixDao.searchByForm(query)
    }

    suspend fun searchRoots(query: String): List<RootEntity> = withContext(Dispatchers.IO) {
        rootDao.searchByForm(query)
    }

    suspend fun searchSuffixes(query: String): List<SuffixEntity> = withContext(Dispatchers.IO) {
        suffixDao.searchByForm(query)
    }

    private fun splitTags(raw: String): List<String> = raw.split(',').map { it.trim() }
}

