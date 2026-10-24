package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PlayerEntity::class, GameResultEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameResultDao(): GameResultDao

    companion object {
        /**
         * И в ЛР №5 (v1), и после добавления длительности раунда (v2)
         * сохраняем всех игроков и старые результаты.
         * Перестройка таблицы нужна из-за различий DEFAULT в предыдущих схемах Room.
         */
        private fun migrateRecords(db: SupportSQLiteDatabase) {
            val columns = mutableSetOf<String>()
            db.query("PRAGMA table_info(`game_results`)").use { cursor ->
                val columnIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) columns.add(cursor.getString(columnIndex))
            }
            val oldDuration = if ("durationSeconds" in columns) {
                "COALESCE(`durationSeconds`, 0)"
            } else "0"

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `game_results_new` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `playerId` INTEGER NOT NULL,
                  `score` INTEGER NOT NULL,
                  `hits` INTEGER NOT NULL,
                  `misses` INTEGER NOT NULL,
                  `accuracy` REAL NOT NULL,
                  `difficulty` TEXT NOT NULL,
                  `playedAt` INTEGER NOT NULL,
                  `durationSeconds` INTEGER NOT NULL,
                  `levelId` INTEGER,
                  `levelName` TEXT,
                  FOREIGN KEY(`playerId`) REFERENCES `players`(`id`)
                      ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `game_results_new`
                   (`id`, `playerId`, `score`, `hits`, `misses`, `accuracy`,
                    `difficulty`, `playedAt`, `durationSeconds`, `levelId`, `levelName`)
                SELECT `id`, `playerId`, `score`, `hits`, `misses`, `accuracy`,
                       `difficulty`, `playedAt`, $oldDuration, NULL, NULL
                FROM `game_results`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `game_results`")
            db.execSQL("ALTER TABLE `game_results_new` RENAME TO `game_results`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_game_results_playerId` ON `game_results` (`playerId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_game_results_levelId` ON `game_results` (`levelId`)")
        }

        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateRecords(db)
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) = migrateRecords(db)
        }

        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bugs_database"
            ).addMigrations(MIGRATION_1_3, MIGRATION_2_3)
                .build().also { instance = it }
        }
    }
}
