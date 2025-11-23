package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "constraints",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["macroId"])]
)
data class ConstraintEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val macroId: Long,
    val constraintType: String, // TIME_RANGE, DAY_OF_WEEK, BATTERY_LEVEL, NETWORK_TYPE, etc.
    val constraintConfig: String, // JSON string with constraint-specific configuration
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

