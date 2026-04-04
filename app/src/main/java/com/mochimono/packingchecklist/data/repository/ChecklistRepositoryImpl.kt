package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.ChecklistDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistWithProgress
import com.mochimono.packingchecklist.data.local.entity.ChecklistEntity
import com.mochimono.packingchecklist.domain.model.Checklist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChecklistRepositoryImpl @Inject constructor(
    private val checklistDao: ChecklistDao
) : ChecklistRepository {

    override fun getAllChecklists(): Flow<List<Checklist>> =
        checklistDao.getAllWithProgress().map { list -> list.map { it.toDomain() } }

    override suspend fun getChecklistById(id: String): Checklist? =
        checklistDao.getById(id)?.toDomain()

    override suspend fun createChecklist(title: String): Checklist {
        val now = System.currentTimeMillis()
        val entity = ChecklistEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = now,
            updatedAt = now
        )
        checklistDao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun deleteChecklist(id: String) =
        checklistDao.deleteById(id)

    override suspend fun updateChecklistTitle(id: String, title: String) =
        checklistDao.updateTitle(id, title, System.currentTimeMillis())

    override suspend fun setSharingEnabled(id: String, enabled: Boolean) =
        checklistDao.updateSharingEnabled(id, enabled, System.currentTimeMillis())
}

private fun ChecklistWithProgress.toDomain() = Checklist(
    id = id,
    title = title,
    createdAt = createdAt,
    isSharingEnabled = isSharingEnabled,
    familyGroupId = familyGroupId,
    checkedCount = checkedCount,
    totalCount = totalCount
)

private fun ChecklistEntity.toDomain() = Checklist(
    id = id,
    title = title,
    createdAt = createdAt,
    isSharingEnabled = isSharingEnabled,
    familyGroupId = familyGroupId
)
