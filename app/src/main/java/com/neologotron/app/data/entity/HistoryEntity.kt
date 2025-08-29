package com.neologotron.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val definition: String,
    val decomposition: String,
    val mode: String, // e.g., technical/poetic/manual
    val timestamp: Long
)

