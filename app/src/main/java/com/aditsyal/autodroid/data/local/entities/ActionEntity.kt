package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["macroId"], name = "index_actions_macro_id"),
        Index(value = ["macroId", "executionOrder"], name = "index_actions_macro_order")
    ]
)
data class ActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val macroId: Long,
    val actionType: String, // WIFI_TOGGLE, BLUETOOTH_TOGGLE, VOLUME_CONTROL, etc.
    val actionConfig: String, // JSON string with action-specific configuration
    val executionOrder: Int,
    val delayAfter: Long = 0, // Delay in milliseconds after action
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

