package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for storing automation rule templates
 * Templates are pre-configured macros that users can apply with one click
 */
@Entity(
    tableName = "templates",
    indices = [
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val category: String,
    val macroJson: String,
    val icon: String? = null,
    val isBuiltIn: Boolean = false,
    val enabled: Boolean = true,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

