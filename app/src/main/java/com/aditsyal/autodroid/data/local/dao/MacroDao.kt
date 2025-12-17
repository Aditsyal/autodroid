package com.aditsyal.autodroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aditsyal.autodroid.data.local.entities.MacroEntity
import com.aditsyal.autodroid.data.local.entities.MacroWithDetails
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY createdAt DESC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Transaction
    @Query("SELECT * FROM macros ORDER BY createdAt DESC")
    fun getAllMacrosWithDetails(): Flow<List<MacroWithDetails>>

    @Query("SELECT * FROM macros WHERE id = :macroId")
    suspend fun getMacroById(macroId: Long): MacroEntity?

    @Query("SELECT * FROM macros WHERE enabled = 1")
    fun getEnabledMacros(): Flow<List<MacroEntity>>

    @Transaction
    @Query("SELECT * FROM macros WHERE id = :macroId")
    suspend fun getMacroWithDetailsById(macroId: Long): MacroWithDetails?

    @Insert
    suspend fun insertMacro(macro: MacroEntity): Long

    @Update
    suspend fun updateMacro(macro: MacroEntity)

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)

    @Query("DELETE FROM macros WHERE id = :macroId")
    suspend fun deleteMacroById(macroId: Long)

    @Query("UPDATE macros SET lastExecuted = :timestamp, executionCount = executionCount + 1 WHERE id = :macroId")
    suspend fun updateExecutionInfo(macroId: Long, timestamp: Long)

    @Query("UPDATE macros SET enabled = :enabled WHERE id = :macroId")
    suspend fun toggleMacro(macroId: Long, enabled: Boolean)
}

