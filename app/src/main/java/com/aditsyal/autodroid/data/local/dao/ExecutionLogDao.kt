package com.aditsyal.autodroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aditsyal.autodroid.data.local.entities.ExecutionLogEntity
import com.aditsyal.autodroid.data.local.entities.ExecutionLogWithMacro
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionLogDao {
    @Query("SELECT * FROM execution_logs WHERE macroId = :macroId ORDER BY executedAt DESC LIMIT :limit")
    fun getExecutionLogsByMacroId(macroId: Long, limit: Int = 50): Flow<List<ExecutionLogEntity>>

    @Query("SELECT * FROM execution_logs ORDER BY executedAt DESC LIMIT :limit")
    fun getAllExecutionLogs(limit: Int = 100): Flow<List<ExecutionLogEntity>>

    @Transaction
    @Query("SELECT * FROM execution_logs ORDER BY executedAt DESC LIMIT :limit")
    fun getAllExecutionLogsWithMacro(limit: Int = 100): Flow<List<ExecutionLogWithMacro>>

    @Insert
    suspend fun insertExecutionLog(log: ExecutionLogEntity): Long

    @Delete
    suspend fun deleteExecutionLog(log: ExecutionLogEntity)

    @Query("DELETE FROM execution_logs WHERE executedAt < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)

    @Query("DELETE FROM execution_logs WHERE macroId = :macroId")
    suspend fun deleteLogsByMacroId(macroId: Long)
}

