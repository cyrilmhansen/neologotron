package com.neologotron.app.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.neologotron.app.data.db.AppDatabase
import com.neologotron.app.data.db.Migrations
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-test.db"

    private fun createDatabaseV5(): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(5) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                "CREATE TABLE IF NOT EXISTS history (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "word TEXT NOT NULL, " +
                                    "definition TEXT NOT NULL, " +
                                    "decomposition TEXT NOT NULL, " +
                                    "mode TEXT NOT NULL, " +
                                    "timestamp INTEGER NOT NULL)",
                            )
                            db.execSQL(
                                "CREATE TABLE IF NOT EXISTS favorites (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "word TEXT NOT NULL, " +
                                    "definition TEXT NOT NULL, " +
                                    "decomposition TEXT NOT NULL, " +
                                    "mode TEXT NOT NULL, " +
                                    "createdAt INTEGER NOT NULL)",
                            )
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                        }
                    },
                )
                .build(),
        )

    @Test
    fun migrate5To6_preservesData() {
        val helperV5 = createDatabaseV5()
        val dbV5 = helperV5.writableDatabase
        dbV5.execSQL(
            "INSERT INTO history (word, definition, decomposition, mode, timestamp) " +
                "VALUES ('mot','def','dec','tech',1)",
        )
        dbV5.execSQL(
            "INSERT INTO favorites (word, definition, decomposition, mode, createdAt) " +
                "VALUES ('mot','def','dec','tech',1)",
        )
        dbV5.close()

        Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(*Migrations.ALL)
            .build()
            .apply {
                openHelper.writableDatabase.close()
            }

        val helperV6 =
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(dbName)
                    .callback(
                        object : SupportSQLiteOpenHelper.Callback(6) {
                            override fun onCreate(db: SupportSQLiteDatabase) {}

                            override fun onUpgrade(
                                db: SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) {}
                        },
                    )
                    .build(),
            )
        val db = helperV6.readableDatabase
        val historyCursor = db.query("SELECT word, prefixForm FROM history")
        assertTrue(historyCursor.moveToFirst())
        assertNull(historyCursor.getString(1))
        historyCursor.close()

        val favoritesCursor = db.query("SELECT word, prefixForm FROM favorites")
        assertTrue(favoritesCursor.moveToFirst())
        assertNull(favoritesCursor.getString(1))
        favoritesCursor.close()
        db.close()
    }
}
