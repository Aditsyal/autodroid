package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "execution_logs",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["macroId"], name = "index_execution_logs_macro_id"),
        Index(value = ["executedAt"], name = "index_execution_logs_executed_at"),
        Index(value = ["macroId", "executedAt"], name = "index_execution_logs_macro_executed")
    ]
)
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val macroId: Long,
    val executedAt: Long = System.currentTimeMillis(),
    val executionStatus: String, // SUCCESS, FAILED, PARTIAL
    val errorMessage: String? = null,
    val executionDurationMs: Long = 0,
    val actionsExecuted: Int = 0
)

