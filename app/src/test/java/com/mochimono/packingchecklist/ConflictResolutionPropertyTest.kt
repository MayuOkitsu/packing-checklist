package com.mochimono.packingchecklist

import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import com.mochimono.packingchecklist.data.remote.FirestoreDataSource
import com.mochimono.packingchecklist.data.repository.SyncManager
import com.mochimono.packingchecklist.data.repository.SyncRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.mockk

// Feature: packing-checklist, Property 21: コンフリクト解決（Last-Write-Wins with serverTimestamp）
class ConflictResolutionPropertyTest : FunSpec({

    val syncManager = SyncManager(
        syncRepository = mockk<SyncRepository>(),
        firestoreDataSource = mockk<FirestoreDataSource>(),
        checklistItemDao = mockk<ChecklistItemDao>()
    )

    fun item(id: String, lastModifiedAt: Long) = ChecklistItemEntity(
        id = id,
        checklistId = "cl",
        name = "item",
        lastModifiedAt = lastModifiedAt
    )

    test("異なるタイムスタンプの場合、最新のタイムスタンプが優先される") {
        checkAll(100, Arb.long(1L..Long.MAX_VALUE / 2), Arb.long(1L..Long.MAX_VALUE / 2)) { ts1, ts2 ->
            val t1 = ts1
            val t2 = ts1 + ts2 // t2 > t1 を保証
            val local = item("idA", t1)
            val remote = item("idB", t2)

            syncManager.resolveConflict(local, remote) shouldBe remote
            syncManager.resolveConflict(remote, local) shouldBe remote
        }
    }

    test("同一タイムスタンプの場合、ドキュメントID辞書順で大きい方が優先される") {
        checkAll(
            100,
            Arb.string(5..20),
            Arb.string(5..20),
            Arb.long(1L..Long.MAX_VALUE)
        ) { id1, id2, ts ->
            val local = item(id1, ts)
            val remote = item(id2, ts)
            val result = syncManager.resolveConflict(local, remote)
            result.id shouldBe maxOf(id1, id2)
        }
    }
})
