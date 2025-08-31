package com.neologotron.app.data.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.neologotron.app.data.dao.PrefixDao
import com.neologotron.app.data.dao.RootDao
import com.neologotron.app.data.dao.SuffixDao
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity

@Singleton
class LexemeRepository
    @Inject
    constructor(
        private val prefixDao: PrefixDao,
        private val rootDao: RootDao,
        private val suffixDao: SuffixDao,
    ) {
        // Simple in-memory caches; invalidated on reseed
        private val mutex = Mutex()
        private var prefixesAll: List<PrefixEntity>? = null
        private var rootsAll: List<RootEntity>? = null
        private var suffixesAll: List<SuffixEntity>? = null
        private val prefixesByTag = mutableMapOf<String, List<PrefixEntity>>()
        private val rootsByTag = mutableMapOf<String, List<RootEntity>>()
        private val suffixesByTag = mutableMapOf<String, List<SuffixEntity>>()

        suspend fun clearCache() =
            mutex.withLock {
                prefixesAll = null
                rootsAll = null
                suffixesAll = null
                prefixesByTag.clear()
                rootsByTag.clear()
                suffixesByTag.clear()
            }

        suspend fun getDistinctTags(): List<String> =
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    val pAll = prefixesAll ?: prefixDao.getAll().also { prefixesAll = it }
                    val rAll = rootsAll ?: rootDao.getAll().also { rootsAll = it }
                    val sAll = suffixesAll ?: suffixDao.getAll().also { suffixesAll = it }
                    val tagSets = mutableSetOf<String>()
                    pAll.forEach { it.tags?.let { t -> tagSets.addAll(splitTags(t)) } }
                    rAll.forEach { it.domain?.let { d -> tagSets.addAll(splitTags(d)) } }
                    sAll.forEach { it.tags?.let { t -> tagSets.addAll(splitTags(t)) } }
                    tagSets.filter { it.isNotBlank() }.sorted()
                }
            }

        suspend fun listPrefixesByTag(tag: String): List<PrefixEntity> =
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    if (tag.isBlank()) {
                        prefixesAll ?: prefixDao.getAll().also { prefixesAll = it }
                    } else {
                        prefixesByTag[tag] ?: prefixDao.findByTag(tag).also { prefixesByTag[tag] = it }
                    }
                }
            }

        suspend fun listRootsByTag(tag: String): List<RootEntity> =
            withContext(Dispatchers.IO) {
                // Using domain as tag-like column for roots
                mutex.withLock {
                    if (tag.isBlank()) {
                        rootsAll ?: rootDao.getAll().also { rootsAll = it }
                    } else {
                        rootsByTag[tag] ?: rootDao.findByDomain(tag).also { rootsByTag[tag] = it }
                    }
                }
            }

        suspend fun listSuffixesByTag(tag: String): List<SuffixEntity> =
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    if (tag.isBlank()) {
                        suffixesAll ?: suffixDao.getAll().also { suffixesAll = it }
                    } else {
                        suffixesByTag[tag] ?: suffixDao.findByTag(tag).also { suffixesByTag[tag] = it }
                    }
                }
            }

        suspend fun searchPrefixes(query: String): List<PrefixEntity> =
            withContext(Dispatchers.IO) {
                prefixDao.searchByForm(query)
            }

        suspend fun searchRoots(query: String): List<RootEntity> =
            withContext(Dispatchers.IO) {
                rootDao.searchByForm(query)
            }

        suspend fun searchSuffixes(query: String): List<SuffixEntity> =
            withContext(Dispatchers.IO) {
                suffixDao.searchByForm(query)
            }

        private fun splitTags(raw: String): List<String> = raw.split(',').map { it.trim() }
    }
