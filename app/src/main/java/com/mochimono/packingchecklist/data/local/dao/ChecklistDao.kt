package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mochimono.packingchecklist.data.local.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

data class ChecklistWithProgress(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSharingEnabled: Boolean,
    val familyGroupId: String?,
    val ownerUserId: String?,
    val checkedCount: Int,
    val totalCount: Int
)

@Dao
interface ChecklistDao {

    @Query(
        """
        SELECT c.*, 
            COALESCE(SUM(CASE WHEN ci.isChecked = 1 THEN 1 ELSE 0 END), 0) AS checkedCount,
            COALESCE(COUNT(ci.id), 0) AS totalCount
        FROM checklists c
        LEFT JOIN checklist_items ci ON c.id = ci.checklistId
        GROUP BY c.id
        ORDER BY c.createdAt DESC
        """
    )
    fun getAllWithProgress(): Flow<List<ChecklistWithProgress>>

    @Query("SELECT * FROM checklists ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklists WHERE id = :id")
    suspend fun getById(id: String): ChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChecklistEntity)

    @Update
    suspend fun update(entity: ChecklistEntity)

    @Query("DELETE FROM checklists WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE checklists SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updatedAt: Long)

    @Query("UPDATE checklists SET isSharingEnabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSharingEnabled(id: String, enabled: Boolean, updatedAt: Long)
}
