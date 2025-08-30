package com.neologotron.app.data.repo

import com.neologotron.app.data.db.AppDatabase
import com.neologotron.app.data.seed.SeedManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val db: AppDatabase,
    private val seed: SeedManager,
    private val lexemes: LexemeRepository,
) {
    suspend fun resetAndReseed() = withContext(Dispatchers.IO) {
        db.historyDao().clear()
        db.prefixDao().clear()
        db.rootDao().clear()
        db.suffixDao().clear()
        // Reset meta by reseeding
        seed.seedAll(db)
        // Invalidate cached indexes
        lexemes.clearCache()
    }
}
