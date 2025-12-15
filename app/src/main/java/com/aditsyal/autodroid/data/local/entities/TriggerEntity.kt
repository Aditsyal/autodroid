package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "triggers",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["macroId"]),
        Index(value = ["enabled", "triggerType"])
    ]
)
data class TriggerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val macroId: Long,
    val triggerType: String, // TIME, LOCATION, SYSTEM_EVENT, APP_EVENT, SENSOR_EVENT
    val triggerConfig: String, // JSON string with trigger-specific configuration
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

