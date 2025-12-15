package com.aditsyal.autodroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aditsyal.autodroid.data.local.entities.TriggerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerDao {
    @Query("SELECT * FROM triggers WHERE macroId = :macroId")
    fun getTriggersByMacroId(macroId: Long): Flow<List<TriggerEntity>>

    @Query("SELECT * FROM triggers WHERE triggerType = :triggerType AND enabled = 1")
    suspend fun getEnabledTriggersByType(triggerType: String): List<TriggerEntity>

    @Query("SELECT * FROM triggers WHERE id = :triggerId")
    suspend fun getTriggerById(triggerId: Long): TriggerEntity?

    @Insert
    suspend fun insertTrigger(trigger: TriggerEntity): Long

    @Update
    suspend fun updateTrigger(trigger: TriggerEntity)

    @Delete
    suspend fun deleteTrigger(trigger: TriggerEntity)

    @Query("DELETE FROM triggers WHERE macroId = :macroId")
    suspend fun deleteTriggersByMacroId(macroId: Long)

    @Query("UPDATE triggers SET enabled = :enabled WHERE id = :triggerId")
    suspend fun toggleTrigger(triggerId: Long, enabled: Boolean)
}

