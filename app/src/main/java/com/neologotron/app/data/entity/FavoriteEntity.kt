package com.neologotron.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val definition: String,
    val decomposition: String,
    val mode: String,
    val createdAt: Long,
    // Optional morph metadata to enable live recompute
    val prefixForm: String? = null,
    val rootForm: String? = null,
    val suffixForm: String? = null,
    val rootGloss: String? = null,
    val rootConnectorPref: String? = null,
    val suffixPosOut: String? = null,
    val suffixDefTemplate: String? = null,
    val suffixTags: String? = null,
)
