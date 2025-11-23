package com.aditsyal.autodroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aditsyal.autodroid.data.local.entities.ActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Query("SELECT * FROM actions WHERE macroId = :macroId ORDER BY executionOrder ASC")
    fun getActionsByMacroId(macroId: Long): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions WHERE id = :actionId")
    suspend fun getActionById(actionId: Long): ActionEntity?

    @Insert
    suspend fun insertAction(action: ActionEntity): Long

    @Update
    suspend fun updateAction(action: ActionEntity)

    @Delete
    suspend fun deleteAction(action: ActionEntity)

    @Query("DELETE FROM actions WHERE macroId = :macroId")
    suspend fun deleteActionsByMacroId(macroId: Long)

    @Query("UPDATE actions SET enabled = :enabled WHERE id = :actionId")
    suspend fun toggleAction(actionId: Long, enabled: Boolean)
}

