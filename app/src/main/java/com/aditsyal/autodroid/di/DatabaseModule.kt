package com.aditsyal.autodroid.di

import android.content.Context
import androidx.room.Room
import com.aditsyal.autodroid.data.local.dao.ActionDao
import com.aditsyal.autodroid.data.local.dao.ConstraintDao
import com.aditsyal.autodroid.data.local.dao.ExecutionLogDao
import com.aditsyal.autodroid.data.local.dao.MacroDao
import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.dao.TriggerDao
import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAutomationDatabase(
        @ApplicationContext context: Context
    ): AutomationDatabase {
        return AutomationDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideMacroDao(database: AutomationDatabase): MacroDao {
        return database.macroDao()
    }

    @Provides
    @Singleton
    fun provideTriggerDao(database: AutomationDatabase): TriggerDao {
        return database.triggerDao()
    }

    @Provides
    @Singleton
    fun provideActionDao(database: AutomationDatabase): ActionDao {
        return database.actionDao()
    }

    @Provides
    @Singleton
    fun provideConstraintDao(database: AutomationDatabase): ConstraintDao {
        return database.constraintDao()
    }

    @Provides
    @Singleton
    fun provideExecutionLogDao(database: AutomationDatabase): ExecutionLogDao {
        return database.executionLogDao()
    }

    @Provides
    @Singleton
    fun provideVariableDao(database: AutomationDatabase): VariableDao {
        return database.variableDao()
    }

    @Provides
    @Singleton
    fun provideTemplateDao(database: AutomationDatabase): TemplateDao {
        return database.templateDao()
    }
}

