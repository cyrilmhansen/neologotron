package com.neologotron.app.data.repo

import com.neologotron.app.data.dao.FavoriteDao
import com.neologotron.app.data.entity.FavoriteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {
    suspend fun add(word: String, definition: String, decomposition: String, mode: String) = withContext(Dispatchers.IO) {
        dao.insert(
            FavoriteEntity(
                word = word,
                definition = definition,
                decomposition = decomposition,
                mode = mode,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun remove(word: String) = withContext(Dispatchers.IO) { dao.deleteByWord(word) }

    suspend fun isFavorited(word: String): Boolean = withContext(Dispatchers.IO) { dao.countByWord(word) > 0 }

    suspend fun list(): List<FavoriteEntity> = withContext(Dispatchers.IO) { dao.listAll() }

    suspend fun get(word: String): FavoriteEntity? = withContext(Dispatchers.IO) { dao.getByWord(word) }
}
