package com.neologotron.app.data.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.neologotron.app.data.dao.HistoryDao
import com.neologotron.app.data.entity.HistoryEntity

@Singleton
class HistoryRepository
    @Inject
    constructor(
        private val dao: HistoryDao,
    ) {
        suspend fun add(
            word: String,
            definition: String,
            decomposition: String,
            mode: String,
            prefixForm: String? = null,
            rootForm: String? = null,
            suffixForm: String? = null,
            rootGloss: String? = null,
            rootConnectorPref: String? = null,
            suffixPosOut: String? = null,
            suffixDefTemplate: String? = null,
            suffixTags: String? = null,
        ) = withContext(Dispatchers.IO) {
            dao.insert(
                HistoryEntity(
                    word = word,
                    definition = definition,
                    decomposition = decomposition,
                    mode = mode,
                    timestamp = System.currentTimeMillis(),
                    prefixForm = prefixForm,
                    rootForm = rootForm,
                    suffixForm = suffixForm,
                    rootGloss = rootGloss,
                    rootConnectorPref = rootConnectorPref,
                    suffixPosOut = suffixPosOut,
                    suffixDefTemplate = suffixDefTemplate,
                    suffixTags = suffixTags,
                ),
            )
        }

        suspend fun recent(limit: Int = 50): List<HistoryEntity> = withContext(Dispatchers.IO) { dao.recent(limit) }

        suspend fun latestByWord(word: String): HistoryEntity? = withContext(Dispatchers.IO) { dao.latestByWord(word) }

        suspend fun delete(id: Long) = withContext(Dispatchers.IO) { dao.deleteById(id) }

        suspend fun insert(entity: HistoryEntity) = withContext(Dispatchers.IO) { dao.insert(entity) }
    }
