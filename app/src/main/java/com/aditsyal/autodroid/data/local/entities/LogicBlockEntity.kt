package com.aditsyal.autodroid.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for storing logic control blocks (if/else, loops)
 * Logic blocks control the flow of action execution within a macro
 */
@Entity(
    tableName = "logic_blocks",
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
        Index(value = ["parentBlockId"]),
        Index(value = ["executionOrder"])
    ]
)
data class LogicBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val macroId: Long,
    val blockType: String, // "IF_CONDITION", "WHILE_LOOP", "FOR_LOOP"
    val condition: String, // JSON string with condition configuration
    val executionOrder: Int, // Order within macro
    val parentBlockId: Long? = null, // For nested blocks
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

