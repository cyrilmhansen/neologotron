package com.neologotron.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prefixes")
data class PrefixEntity(
    @PrimaryKey val id: String,
    val form: String,
    val altForms: String?,
    val gloss: String,
    val origin: String?,
    val connector: String?,
    val phonRules: String?,
    val tags: String?,
    val weight: Double?,
)
