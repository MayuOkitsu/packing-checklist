package com.mochimono.packingchecklist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {

    @Query("SELECT * FROM checklist_items WHERE checklistId = :checklistId ORDER BY sortOrder ASC")
    fun getByChecklistId(checklistId: String): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE id = :id")
    suspend fun getById(id: String): ChecklistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChecklistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ChecklistItemEntity>)

    @Update
    suspend fun update(entity: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE checklist_items SET isChecked = :isChecked, bagId = :bagId, lastModifiedAt = :lastModifiedAt WHERE id = :id")
    suspend fun updateCheckState(id: String, isChecked: Boolean, bagId: String?, lastModifiedAt: Long)

    @Query("UPDATE checklist_items SET bagId = NULL WHERE bagId = :bagId")
    suspend fun clearBagAssignment(bagId: String)

    @Query("UPDATE checklist_items SET name = :name, lastModifiedAt = :lastModifiedAt WHERE id = :id")
    suspend fun updateName(id: String, name: String, lastModifiedAt: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM checklist_items WHERE checklistId = :checklistId")
    suspend fun getMaxSortOrder(checklistId: String): Int

    @Query("""
        SELECT ci.*, b.id AS bag_id, b.name AS bag_name 
        FROM checklist_items ci 
        LEFT JOIN bags b ON ci.bagId = b.id 
        WHERE ci.checklistId = :checklistId 
        ORDER BY ci.sortOrder ASC
    """)
    fun getByChecklistIdWithBag(checklistId: String): Flow<List<ChecklistItemWithBag>>
}

data class ChecklistItemWithBag(
    val id: String,
    val checklistId: String,
    val name: String,
    val isChecked: Boolean,
    val bagId: String?,
    val lastModifiedAt: Long,
    val sortOrder: Int,
    @androidx.room.ColumnInfo(name = "bag_id") val bagJoinId: String?,
    @androidx.room.ColumnInfo(name = "bag_name") val bagJoinName: String?
)
