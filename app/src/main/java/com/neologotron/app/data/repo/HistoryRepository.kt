package com.neologotron.app.data.repo

import com.neologotron.app.data.dao.HistoryDao
import com.neologotron.app.data.entity.HistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: HistoryDao
) {
    suspend fun add(word: String, definition: String, decomposition: String, mode: String) = withContext(Dispatchers.IO) {
        dao.insert(
            HistoryEntity(
                word = word,
                definition = definition,
                decomposition = decomposition,
                mode = mode,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun recent(limit: Int = 50): List<HistoryEntity> = withContext(Dispatchers.IO) { dao.recent(limit) }

    suspend fun latestByWord(word: String): HistoryEntity? = withContext(Dispatchers.IO) { dao.latestByWord(word) }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) { dao.deleteById(id) }

    suspend fun insert(entity: HistoryEntity) = withContext(Dispatchers.IO) { dao.insert(entity) }
}
