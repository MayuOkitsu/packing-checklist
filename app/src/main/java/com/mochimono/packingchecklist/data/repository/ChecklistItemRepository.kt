package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.domain.model.ChecklistItem
import kotlinx.coroutines.flow.Flow

interface ChecklistItemRepository {
    fun getItemsByChecklistId(checklistId: String): Flow<List<ChecklistItem>>
    suspend fun addItem(checklistId: String, name: String): ChecklistItem
    suspend fun deleteItem(id: String)
    suspend fun updateItemName(id: String, name: String)
    suspend fun toggleCheck(id: String, bagId: String?): ChecklistItem
    suspend fun uncheckItem(id: String): ChecklistItem
    suspend fun clearBagAssignments(bagId: String)
}
