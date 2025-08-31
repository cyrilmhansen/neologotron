package com.neologotron.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neologotron.app.data.entity.SuffixEntity

@Dao
interface SuffixDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SuffixEntity>)

    @Query("SELECT COUNT(*) FROM suffixes")
    suspend fun count(): Int

    @Query(
        "SELECT * FROM suffixes ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun getAll(): List<SuffixEntity>

    @Query(
        "SELECT * FROM suffixes WHERE tags LIKE '%' || :tag || '%' ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun findByTag(tag: String): List<SuffixEntity>

    @Query(
        "SELECT * FROM suffixes WHERE form LIKE '%' || :query || '%' ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun searchByForm(query: String): List<SuffixEntity>

    @Query("DELETE FROM suffixes")
    suspend fun clear()
}
