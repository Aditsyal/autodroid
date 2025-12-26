package com.aditsyal.autodroid.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aditsyal.autodroid.data.local.dao.ActionDao
import com.aditsyal.autodroid.data.local.dao.ConstraintDao
import com.aditsyal.autodroid.data.local.dao.ExecutionLogDao
import com.aditsyal.autodroid.data.local.dao.MacroDao
import com.aditsyal.autodroid.data.local.dao.TriggerDao
import com.aditsyal.autodroid.data.local.entities.ActionEntity
import com.aditsyal.autodroid.data.local.entities.ConstraintEntity
import com.aditsyal.autodroid.data.local.entities.ExecutionLogEntity
import com.aditsyal.autodroid.data.local.entities.MacroEntity
import com.aditsyal.autodroid.data.local.entities.TriggerEntity

@Database(
    entities = [
        MacroEntity::class,
        TriggerEntity::class,
        ActionEntity::class,
        ConstraintEntity::class,
        ExecutionLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AutomationDatabase : RoomDatabase() {
    abstract fun macroDao(): MacroDao
    abstract fun triggerDao(): TriggerDao
    abstract fun actionDao(): ActionDao
    abstract fun constraintDao(): ConstraintDao
    abstract fun executionLogDao(): ExecutionLogDao

    companion object {
        private const val DATABASE_NAME = "automation_database.db"

        fun getDatabase(context: Context): AutomationDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AutomationDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

