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
}
