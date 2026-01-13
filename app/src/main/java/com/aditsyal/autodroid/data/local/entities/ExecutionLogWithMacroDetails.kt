package com.aditsyal.autodroid.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ExecutionLogWithMacroDetails(
    @Embedded val log: ExecutionLogEntity,
    @Relation(
        parentColumn = "macroId",
        entityColumn = "id"
    )
    val macro: MacroEntity?,
    @Relation(
        parentColumn = "macroId",
        entityColumn = "macroId"
    )
    val triggers: List<TriggerEntity>,
    @Relation(
        parentColumn = "macroId",
        entityColumn = "macroId"
    )
    val actions: List<ActionEntity>,
    @Relation(
        parentColumn = "macroId",
        entityColumn = "macroId"
    )
    val constraints: List<ConstraintEntity>
)