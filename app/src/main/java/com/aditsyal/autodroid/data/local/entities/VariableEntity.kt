package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "variables",
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
        Index(value = ["name", "scope"]),
        Index(value = ["scope"])
    ]
)
data class VariableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val value: String, // Stored as string, can represent number, boolean, or string
    val scope: String, // "LOCAL" or "GLOBAL"
    val macroId: Long? = null, // null for global variables, set for local variables
    val type: String = "STRING", // "STRING", "NUMBER", "BOOLEAN"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

