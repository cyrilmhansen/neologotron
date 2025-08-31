package com.neologotron.app.data.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton
import androidx.room.Room
import com.neologotron.app.data.dao.FavoriteDao
import com.neologotron.app.data.dao.HistoryDao
import com.neologotron.app.data.dao.PrefixDao
import com.neologotron.app.data.dao.RootDao
import com.neologotron.app.data.dao.SuffixDao
import com.neologotron.app.data.db.AppDatabase
import com.neologotron.app.data.seed.SeedManager

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        seedManager: SeedManager,
    ): AppDatabase {
        val db =
            Room.databaseBuilder(context, AppDatabase::class.java, "neologotron.db")
                .fallbackToDestructiveMigration() // safe for early MVP
                .build()
        // Seed in background on first run
        Executors.newSingleThreadExecutor().execute {
            try {
                runCatching { kotlinx.coroutines.runBlocking { seedManager.seedIfEmpty(db) } }.onFailure {
                    Log.e("DatabaseSeed", "Seeding failed", it)
                }
            } catch (t: Throwable) {
                Log.e("DatabaseSeed", "Unexpected seeding error", t)
            }
        }
        return db
    }

    @Provides
    fun providePrefixDao(db: AppDatabase): PrefixDao = db.prefixDao()

    @Provides
    fun provideRootDao(db: AppDatabase): RootDao = db.rootDao()

    @Provides
    fun provideSuffixDao(db: AppDatabase): SuffixDao = db.suffixDao()

    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
}
