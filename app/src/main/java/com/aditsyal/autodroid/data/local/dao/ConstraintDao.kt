package com.aditsyal.autodroid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aditsyal.autodroid.data.local.entities.ConstraintEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConstraintDao {
    @Query("SELECT * FROM constraints WHERE macroId = :macroId")
    fun getConstraintsByMacroId(macroId: Long): Flow<List<ConstraintEntity>>

    @Query("SELECT * FROM constraints WHERE id = :constraintId")
    suspend fun getConstraintById(constraintId: Long): ConstraintEntity?

    @Insert
    suspend fun insertConstraint(constraint: ConstraintEntity): Long

    @Update
    suspend fun updateConstraint(constraint: ConstraintEntity)

    @Delete
    suspend fun deleteConstraint(constraint: ConstraintEntity)

    @Query("DELETE FROM constraints WHERE macroId = :macroId")
    suspend fun deleteConstraintsByMacroId(macroId: Long)

    @Query("UPDATE constraints SET enabled = :enabled WHERE id = :constraintId")
    suspend fun toggleConstraint(constraintId: Long, enabled: Boolean)
}

