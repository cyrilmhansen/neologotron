package com.neologotron.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neologotron.app.data.entity.RootEntity

@Dao
interface RootDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RootEntity>)

    @Query("SELECT COUNT(*) FROM roots")
    suspend fun count(): Int

    @Query(
        "SELECT * FROM roots ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun getAll(): List<RootEntity>

    @Query(
        "SELECT * FROM roots WHERE domain LIKE '%' || :tag || '%' OR :tag = '' ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun findByDomain(tag: String): List<RootEntity>

    @Query(
        "SELECT * FROM roots WHERE form LIKE '%' || :query || '%' ORDER BY (CASE WHEN weight IS NULL THEN 1 ELSE 0 END), weight DESC, id",
    )
    suspend fun searchByForm(query: String): List<RootEntity>

    @Query("DELETE FROM roots")
    suspend fun clear()
}
