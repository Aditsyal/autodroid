package com.aditsyal.autodroid.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 5,
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

