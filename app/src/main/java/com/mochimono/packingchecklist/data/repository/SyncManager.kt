package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import com.mochimono.packingchecklist.data.local.entity.PendingSyncEntity
import com.mochimono.packingchecklist.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val syncRepository: SyncRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val checklistItemDao: ChecklistItemDao
) {

    suspend fun enqueueSync(
        entityType: String,
        entityId: String,
        operation: String,
        payload: String
    ) {
        syncRepository.enqueue(
            PendingSyncEntity(
                id = UUID.randomUUID().toString(),
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payload = payload,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun processPendingSyncs(groupId: String) {
        val unsynced = syncRepository.getUnsynced()
        for (entry in unsynced) {
            try {
                when (entry.entityType) {
                    "checklist_item" -> processItemSync(groupId, entry)
                }
                syncRepository.markSynced(entry.id)
            } catch (_: Exception) {
                // リトライは次回のprocessPendingSyncs呼び出しで行う
            }
        }
    }

    private suspend fun processItemSync(groupId: String, entry: PendingSyncEntity) {
        // payloadはJSON: checklistId,name,isChecked,bagId等を含む想定
        // 簡易的にentityIdからchecklistIdを分離せず、payloadにchecklistIdを含める設計
        val parts = entry.payload.split("|")
        val checklistId = parts.getOrNull(0) ?: return
        when (entry.operation) {
            "delete" -> firestoreDataSource.deleteItem(groupId, checklistId, entry.entityId)
            else -> {
                val item = checklistItemDao.getById(entry.entityId) ?: return
                firestoreDataSource.setItem(
                    groupId, checklistId, item.id,
                    mapOf(
                        "name" to item.name,
                        "isChecked" to item.isChecked,
                        "bagId" to (item.bagId ?: ""),
                        "sortOrder" to item.sortOrder
                    )
                )
            }
        }
    }

    fun resolveConflict(
        local: ChecklistItemEntity,
        remote: ChecklistItemEntity
    ): ChecklistItemEntity {
        return when {
            remote.lastModifiedAt > local.lastModifiedAt -> remote
            remote.lastModifiedAt < local.lastModifiedAt -> local
            else -> if (remote.id >= local.id) remote else local
        }
    }

    fun observeRemoteChanges(
        groupId: String,
        checklistId: String
    ): Flow<List<ChecklistItemEntity>> =
        firestoreDataSource.observeItems(groupId, checklistId).map { snapshot ->
            snapshot.documents.map { doc ->
                ChecklistItemEntity(
                    id = doc.id,
                    checklistId = checklistId,
                    name = doc.getString("name") ?: "",
                    isChecked = doc.getBoolean("isChecked") ?: false,
                    bagId = doc.getString("bagId")?.ifEmpty { null },
                    lastModifiedAt = doc.getTimestamp("lastModifiedAt")
                        ?.toDate()?.time ?: 0L,
                    sortOrder = (doc.getLong("sortOrder") ?: 0).toInt()
                )
            }
        }

    suspend fun mergeRemoteItems(
        checklistId: String,
        remoteItems: List<ChecklistItemEntity>
    ) {
        for (remote in remoteItems) {
            val local = checklistItemDao.getById(remote.id)
            val winner = if (local != null) resolveConflict(local, remote) else remote
            checklistItemDao.insert(winner)
        }
    }
}
