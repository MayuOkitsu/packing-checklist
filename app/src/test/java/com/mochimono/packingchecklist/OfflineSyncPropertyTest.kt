package com.mochimono.packingchecklist

import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import com.mochimono.packingchecklist.data.local.entity.PendingSyncEntity
import com.mochimono.packingchecklist.data.remote.FirestoreDataSource
import com.mochimono.packingchecklist.data.repository.SyncManager
import com.mochimono.packingchecklist.data.repository.SyncRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs

// Feature: packing-checklist, Property 22: オフライン変更の同期
class OfflineSyncPropertyTest : FunSpec({

    test("PendingSyncにエンキューされた全変更がprocessPendingSyncs後に同期済みになる") {
        checkAll(
            100,
            Arb.list(Arb.string(5..15), 1..10).map { ids -> ids.distinct() }
        ) { entityIds ->
            val syncRepository = mockk<SyncRepository>()
            val firestoreDataSource = mockk<FirestoreDataSource>()
            val checklistItemDao = mockk<ChecklistItemDao>()

            val pendingEntries = entityIds.mapIndexed { i, entityId ->
                PendingSyncEntity(
                    id = "sync_$i",
                    entityType = "checklist_item",
                    entityId = entityId,
                    operation = "update",
                    payload = """{"checklistId":"cl1"}""",
                    timestamp = System.currentTimeMillis() + i,
                    isSynced = false
                )
            }

            coEvery { syncRepository.getUnsynced() } returns pendingEntries
            coEvery { syncRepository.markSynced(any()) } just Runs

            pendingEntries.forEach { entry ->
                coEvery { checklistItemDao.getById(entry.entityId) } returns ChecklistItemEntity(
                    id = entry.entityId,
                    checklistId = "cl1",
                    name = "item_${entry.entityId}",
                    sortOrder = 0
                )
            }
            coEvery { firestoreDataSource.setItem(any(), any(), any(), any()) } just Runs

            val syncManager = SyncManager(syncRepository, firestoreDataSource, checklistItemDao)
            syncManager.processPendingSyncs("group1")

            pendingEntries.forEach { entry ->
                coVerify(exactly = 1) { syncRepository.markSynced(entry.id) }
            }
        }
    }
})
