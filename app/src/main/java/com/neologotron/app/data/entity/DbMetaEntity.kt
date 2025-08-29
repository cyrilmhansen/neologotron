package com.neologotron.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "db_meta")
data class DbMetaEntity(
    @PrimaryKey val id: Int = 1,
    val createdAtMillis: Long,
    val version: Int
)

