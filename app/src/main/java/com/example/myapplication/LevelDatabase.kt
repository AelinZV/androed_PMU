package com.example.myapplication

import android.content.Context
import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "custom_levels")
data class LevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val season: String,
    val maxBugs: Int,
    val speed: Int,
    val durationSeconds: Int,
    val commonBugId: String,
    val rareBugId: String,
    val veryRareBugId: String,
    @ColumnInfo(defaultValue = "'ultrarare1'") val ultraRareBugId: String
) {
    fun toConfig() = LevelConfig(
        id, name, Season.valueOf(season), maxBugs, speed, durationSeconds,
        commonBugId, rareBugId, veryRareBugId, ultraRareBugId
    )
    companion object {
        fun from(config: LevelConfig) = LevelEntity(
            config.id, config.name, config.season.name, config.maxBugs, config.speed,
            config.durationSeconds, config.commonBugId, config.rareBugId,
            config.veryRareBugId, config.ultraRareBugId
        )
    }
}

@Dao
interface LevelDao {
    @Query("SELECT * FROM custom_levels ORDER BY id DESC")
    fun listAll(): List<LevelEntity>

    @Query("SELECT * FROM custom_levels WHERE id = :id LIMIT 1")
    fun getById(id: Long): LevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(level: LevelEntity): Long

    @Query("DELETE FROM custom_levels WHERE id = :id")
    fun delete(id: Long)
}

/** Отдельный файл Room: текущая bugs_database с игроками и рекордами не меняется. */
@Database(entities = [LevelEntity::class], version = 2, exportSchema = false)
abstract class LevelDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
    companion object {
        /** Миграция существующих levels_database версии 1 без удаления уровней. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE custom_levels " +
                        "ADD COLUMN ultraRareBugId TEXT NOT NULL DEFAULT 'ultrarare1'"
                )
                // В прежней версии ultrarare были частью суперредкого слота.
                // Переносим выбранный вид в новый слот, сохраняя предпочтение.
                db.execSQL(
                    "UPDATE custom_levels " +
                        "SET ultraRareBugId = veryRareBugId, veryRareBugId = 'superrare1' " +
                        "WHERE veryRareBugId IN ('ultrarare1', 'ultrarare2')"
                )
            }
        }

        @Volatile private var instance: LevelDatabase? = null
        fun get(context: Context): LevelDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, LevelDatabase::class.java, "levels_database"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
