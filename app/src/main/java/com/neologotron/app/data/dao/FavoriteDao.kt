package com.neologotron.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neologotron.app.data.entity.FavoriteEntity

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE word = :word")
    suspend fun deleteByWord(word: String)

    @Query("SELECT COUNT(*) FROM favorites WHERE word = :word")
    suspend fun countByWord(word: String): Int

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    suspend fun listAll(): List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE word = :word LIMIT 1")
    suspend fun getByWord(word: String): FavoriteEntity?

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM favorites WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FavoriteEntity?
}
