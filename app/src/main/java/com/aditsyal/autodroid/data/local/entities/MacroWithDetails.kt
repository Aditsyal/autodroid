package com.aditsyal.autodroid.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class MacroWithDetails(
    @Embedded val macro: MacroEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "macroId"
    )
    val triggers: List<TriggerEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "macroId"
    )
    val actions: List<ActionEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "macroId"
    )
    val constraints: List<ConstraintEntity>
)
