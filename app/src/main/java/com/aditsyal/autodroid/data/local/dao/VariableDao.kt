package com.aditsyal.autodroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aditsyal.autodroid.data.local.entities.VariableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableDao {
    @Query("SELECT * FROM variables")
    fun getAllVariables(): Flow<List<VariableEntity>>

    @Query("SELECT * FROM variables WHERE macroId = :macroId")
    fun getVariablesByMacroId(macroId: Long): Flow<List<VariableEntity>>

    @Query("SELECT * FROM variables WHERE scope = 'GLOBAL'")
    fun getAllGlobalVariables(): Flow<List<VariableEntity>>

    @Query("SELECT * FROM variables WHERE name = :name AND scope = :scope AND (macroId = :macroId OR scope = 'GLOBAL')")
    suspend fun getVariable(name: String, scope: String, macroId: Long?): VariableEntity?

    @Query("SELECT * FROM variables WHERE id = :variableId")
    suspend fun getVariableById(variableId: Long): VariableEntity?

    @Insert
    suspend fun insertVariable(variable: VariableEntity): Long

    @Update
    suspend fun updateVariable(variable: VariableEntity)

    @Delete
    suspend fun deleteVariable(variable: VariableEntity)

    @Query("DELETE FROM variables WHERE macroId = :macroId")
    suspend fun deleteVariablesByMacroId(macroId: Long)

    @Query("DELETE FROM variables WHERE scope = 'GLOBAL' AND name = :name")
    suspend fun deleteGlobalVariable(name: String)

    // Synchronous methods for import/export operations
    @Query("SELECT * FROM variables WHERE scope = 'GLOBAL'")
    fun getAllGlobalVariablesSync(): List<VariableEntity>

    @Query("SELECT * FROM variables WHERE name = :name AND scope = 'GLOBAL'")
    fun getGlobalVariableByNameSync(name: String): VariableEntity?
}

