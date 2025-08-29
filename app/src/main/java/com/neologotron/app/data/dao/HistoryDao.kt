package com.neologotron.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neologotron.app.data.entity.HistoryEntity

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int = 50): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE word = :word ORDER BY timestamp DESC LIMIT 1")
    suspend fun latestByWord(word: String): HistoryEntity?

    @Query("DELETE FROM history")
    suspend fun clear()
}
