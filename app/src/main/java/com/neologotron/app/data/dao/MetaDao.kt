package com.neologotron.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.neologotron.app.data.entity.DbMetaEntity

@Dao
interface MetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meta: DbMetaEntity)

    @Query("SELECT * FROM db_meta WHERE id = 1 LIMIT 1")
    suspend fun get(): DbMetaEntity?
}

