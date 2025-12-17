package com.aditsyal.autodroid.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ExecutionLogWithMacro(
    @Embedded val log: ExecutionLogEntity,
    @Relation(
        parentColumn = "macroId",
        entityColumn = "id"
    )
    val macro: MacroEntity?
)
