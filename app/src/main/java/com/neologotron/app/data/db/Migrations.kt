package com.neologotron.app.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val FROM_5_TO_6 =
        object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add nullable morph metadata columns to history table
                db.execSQL("ALTER TABLE history ADD COLUMN prefixForm TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN rootForm TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN suffixForm TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN rootGloss TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN rootConnectorPref TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN suffixPosOut TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN suffixDefTemplate TEXT")
                db.execSQL("ALTER TABLE history ADD COLUMN suffixTags TEXT")

                // Add nullable morph metadata columns to favorites table
                db.execSQL("ALTER TABLE favorites ADD COLUMN prefixForm TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN rootForm TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN suffixForm TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN rootGloss TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN rootConnectorPref TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN suffixPosOut TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN suffixDefTemplate TEXT")
                db.execSQL("ALTER TABLE favorites ADD COLUMN suffixTags TEXT")
            }
        }

    val ALL = arrayOf(FROM_5_TO_6)
}
