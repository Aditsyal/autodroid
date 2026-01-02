package com.aditsyal.autodroid.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
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

        fun getDatabase(context: Context): AutomationDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AutomationDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

