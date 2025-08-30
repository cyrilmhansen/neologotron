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
            FavoriteEntity(
                word = word,
                definition = definition,
                decomposition = decomposition,
                mode = mode,
                createdAt = System.currentTimeMillis(),
                prefixForm = prefixForm,
                rootForm = rootForm,
                suffixForm = suffixForm,
                rootGloss = rootGloss,
                rootConnectorPref = rootConnectorPref,
                suffixPosOut = suffixPosOut,
                suffixDefTemplate = suffixDefTemplate,
                suffixTags = suffixTags,
            )
        )
    }

    suspend fun remove(word: String) = withContext(Dispatchers.IO) { dao.deleteByWord(word) }

    suspend fun isFavorited(word: String): Boolean = withContext(Dispatchers.IO) { dao.countByWord(word) > 0 }

    suspend fun list(): List<FavoriteEntity> = withContext(Dispatchers.IO) { dao.listAll() }

    suspend fun get(word: String): FavoriteEntity? = withContext(Dispatchers.IO) { dao.getByWord(word) }

    suspend fun removeById(id: Long) = withContext(Dispatchers.IO) { dao.deleteById(id) }

    suspend fun insert(entity: FavoriteEntity) = withContext(Dispatchers.IO) { dao.insert(entity) }
}
