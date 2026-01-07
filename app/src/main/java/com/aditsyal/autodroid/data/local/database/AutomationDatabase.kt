package com.aditsyal.autodroid.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aditsyal.autodroid.BuildConfig
import com.aditsyal.autodroid.data.local.dao.ActionDao
import com.aditsyal.autodroid.data.local.dao.ConstraintDao
import com.aditsyal.autodroid.data.local.dao.ExecutionLogDao
import com.aditsyal.autodroid.data.local.dao.MacroDao
import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.dao.TriggerDao
import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.entities.ActionEntity
import com.aditsyal.autodroid.data.local.entities.ConstraintEntity
import com.aditsyal.autodroid.data.local.entities.ExecutionLogEntity
import com.aditsyal.autodroid.data.local.entities.LogicBlockEntity
import com.aditsyal.autodroid.data.local.entities.MacroEntity
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import com.aditsyal.autodroid.data.local.entities.TriggerEntity
import com.aditsyal.autodroid.data.local.entities.VariableEntity
import timber.log.Timber
import java.util.concurrent.Executors

@Database(
    entities = [
        MacroEntity::class,
        TriggerEntity::class,
        ActionEntity::class,
        ConstraintEntity::class,
        ExecutionLogEntity::class,
        VariableEntity::class,
        LogicBlockEntity::class,
        TemplateEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AutomationDatabase : RoomDatabase() {
    abstract fun macroDao(): MacroDao
    abstract fun triggerDao(): TriggerDao
    abstract fun actionDao(): ActionDao
    abstract fun constraintDao(): ConstraintDao
    abstract fun executionLogDao(): ExecutionLogDao
    abstract fun variableDao(): VariableDao
    abstract fun templateDao(): TemplateDao

    companion object {
        private const val DATABASE_NAME = "automation_database.db"

        // Migrations
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic here if schema changed from 1 to 2
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic here if schema changed from 2 to 3
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic here if schema changed from 3 to 4
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic here if schema changed from 4 to 5
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic here if schema changed from 5 to 6
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_triggers_enabled ON triggers(enabled)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_triggers_macro_id ON triggers(macroId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_triggers_type_enabled ON triggers(triggerType, enabled)"
                )
            }
        }

        fun getDatabase(context: Context): AutomationDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AutomationDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .setQueryExecutor(Executors.newFixedThreadPool(4))
                .setTransactionExecutor(Executors.newSingleThreadExecutor())
                .setQueryCallback({ sqlQuery, bindArgs ->
                    if (BuildConfig.DEBUG) {
                        Timber.d("Room Query: $sqlQuery, Args: $bindArgs")
                    }
                }, Executors.newSingleThreadExecutor())
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }
    }
}

