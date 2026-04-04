package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.BagDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
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
        checklistItemDao.getByChecklistId(checklistId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addItem(checklistId: String, name: String): ChecklistItem {
        val now = System.currentTimeMillis()
        val entity = ChecklistItemEntity(
            id = UUID.randomUUID().toString(),
            checklistId = checklistId,
            name = name,
            lastModifiedAt = now
        )
        checklistItemDao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun deleteItem(id: String) =
        checklistItemDao.deleteById(id)

    override suspend fun updateItemName(id: String, name: String) =
        checklistItemDao.updateName(id, name, System.currentTimeMillis())

    override suspend fun toggleCheck(id: String, bagId: String?): ChecklistItem {
        val now = System.currentTimeMillis()
        checklistItemDao.updateCheckState(id, isChecked = true, bagId = bagId, lastModifiedAt = now)
        return checklistItemDao.getById(id)!!.toDomain()
    }

    override suspend fun uncheckItem(id: String): ChecklistItem {
        val now = System.currentTimeMillis()
        checklistItemDao.updateCheckState(id, isChecked = false, bagId = null, lastModifiedAt = now)
        return checklistItemDao.getById(id)!!.toDomain()
    }

    override suspend fun clearBagAssignments(bagId: String) =
        checklistItemDao.clearBagAssignment(bagId)

    private suspend fun ChecklistItemEntity.toDomain(): ChecklistItem {
        val bag = bagId?.let { bagDao.getById(it) }
        return ChecklistItem(
            id = id,
            checklistId = checklistId,
            name = name,
            isChecked = isChecked,
            bag = bag?.let { Bag(it.id, it.name) },
            lastModifiedAt = lastModifiedAt
        )
    }
}
