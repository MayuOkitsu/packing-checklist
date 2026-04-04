package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.BagDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistItemWithBag
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import com.mochimono.packingchecklist.domain.model.Bag
import com.mochimono.packingchecklist.domain.model.ChecklistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChecklistItemRepositoryImpl @Inject constructor(
    private val checklistItemDao: ChecklistItemDao,
    private val bagDao: BagDao
) : ChecklistItemRepository {

    override fun getItemsByChecklistId(checklistId: String): Flow<List<ChecklistItem>> =
        checklistItemDao.getByChecklistIdWithBag(checklistId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addItem(checklistId: String, name: String): ChecklistItem {
        val now = System.currentTimeMillis()
        val nextSort = checklistItemDao.getMaxSortOrder(checklistId) + 1
        val entity = ChecklistItemEntity(
            id = UUID.randomUUID().toString(),
            checklistId = checklistId,
            name = name,
            lastModifiedAt = now,
            sortOrder = nextSort
        )
        checklistItemDao.insert(entity)
        return ChecklistItem(
            id = entity.id, checklistId = checklistId, name = name,
            isChecked = false, bag = null, lastModifiedAt = now
        )
    }

    override suspend fun deleteItem(id: String) =
        checklistItemDao.deleteById(id)

    override suspend fun updateItemName(id: String, name: String) =
        checklistItemDao.updateName(id, name, System.currentTimeMillis())

    override suspend fun toggleCheck(id: String, bagId: String?): ChecklistItem {
        val now = System.currentTimeMillis()
        checklistItemDao.updateCheckState(id, isChecked = true, bagId = bagId, lastModifiedAt = now)
        val entity = checklistItemDao.getById(id)!!
        val bag = bagId?.let { bagDao.getById(it) }
        return ChecklistItem(
            id = entity.id, checklistId = entity.checklistId, name = entity.name,
            isChecked = true, bag = bag?.let { Bag(it.id, it.name) }, lastModifiedAt = now
        )
    }

    override suspend fun uncheckItem(id: String): ChecklistItem {
        val now = System.currentTimeMillis()
        checklistItemDao.updateCheckState(id, isChecked = false, bagId = null, lastModifiedAt = now)
        val entity = checklistItemDao.getById(id)!!
        return ChecklistItem(
            id = entity.id, checklistId = entity.checklistId, name = entity.name,
            isChecked = false, bag = null, lastModifiedAt = now
        )
    }

    override suspend fun clearBagAssignments(bagId: String) =
        checklistItemDao.clearBagAssignment(bagId)

    private fun ChecklistItemWithBag.toDomain() = ChecklistItem(
        id = id, checklistId = checklistId, name = name, isChecked = isChecked,
        bag = bagJoinId?.let { Bag(it, bagJoinName ?: "") },
        lastModifiedAt = lastModifiedAt
    )
}
