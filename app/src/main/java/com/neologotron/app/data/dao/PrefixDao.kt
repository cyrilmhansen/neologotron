package com.neologotron.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neologotron.app.data.entity.PrefixEntity

@Dao
interface PrefixDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PrefixEntity>)

    @Query("SELECT COUNT(*) FROM prefixes")
    suspend fun count(): Int

    @Query(
        "SELECT * FROM prefixes ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun getAll(): List<PrefixEntity>

    @Query(
        "SELECT * FROM prefixes WHERE tags LIKE '%' || :tag || '%' ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun findByTag(tag: String): List<PrefixEntity>

    @Query(
        "SELECT * FROM prefixes WHERE form LIKE '%' || :query || '%' ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun searchByForm(query: String): List<PrefixEntity>

    @Query("DELETE FROM prefixes")
    suspend fun clear()
}
