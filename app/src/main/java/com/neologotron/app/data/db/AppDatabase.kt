package com.neologotron.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neologotron.app.data.dao.PrefixDao
import com.neologotron.app.data.dao.RootDao
import com.neologotron.app.data.dao.SuffixDao
import com.neologotron.app.data.dao.HistoryDao
import com.neologotron.app.data.dao.MetaDao
import com.neologotron.app.data.dao.FavoriteDao
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.data.entity.HistoryEntity
import com.neologotron.app.data.entity.DbMetaEntity
import com.neologotron.app.data.entity.FavoriteEntity

@Database(
    entities = [PrefixEntity::class, RootEntity::class, SuffixEntity::class, HistoryEntity::class, DbMetaEntity::class, FavoriteEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prefixDao(): PrefixDao
    abstract fun rootDao(): RootDao
    abstract fun suffixDao(): SuffixDao
    abstract fun historyDao(): HistoryDao
    abstract fun metaDao(): MetaDao
    abstract fun favoriteDao(): FavoriteDao
}
