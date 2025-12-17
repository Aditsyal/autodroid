package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "macros",
    indices = [
        Index(value = ["enabled"]),
        Index(value = ["name"])
    ]
)
data class MacroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastExecuted: Long? = null,
    val executionCount: Int = 0
)

