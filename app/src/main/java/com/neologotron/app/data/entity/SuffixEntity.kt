package com.neologotron.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suffixes")
data class SuffixEntity(
    @PrimaryKey val id: String,
    val form: String,
    val altForms: String?,
    val gloss: String,
    val origin: String?,
    val posOut: String?,
    val defTemplate: String?,
    val tags: String?,
    val weight: Double?,
)
