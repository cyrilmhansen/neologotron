package com.neologotron.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roots")
data class RootEntity(
    @PrimaryKey val id: String,
    val form: String,
    val altForms: String?,
    val gloss: String,
    val origin: String?,
    val domain: String?,
    val connectorPref: String?,
    val examples: String?,
    val weight: Double?
)

