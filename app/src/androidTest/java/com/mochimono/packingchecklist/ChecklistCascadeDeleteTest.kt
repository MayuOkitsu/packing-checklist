package com.mochimono.packingchecklist

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mochimono.packingchecklist.data.local.dao.ChecklistDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.db.PackingChecklistDatabase
import com.mochimono.packingchecklist.data.local.entity.ChecklistEntity
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Feature: packing-checklist, Property 10: Checklist削除のカスケード
@RunWith(AndroidJUnit4::class)
class ChecklistCascadeDeleteTest {

    private lateinit var db: PackingChecklistDatabase
    private lateinit var checklistDao: ChecklistDao
    private lateinit var itemDao: ChecklistItemDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PackingChecklistDatabase::class.java
        ).allowMainThreadQueries().build()
        checklistDao = db.checklistDao()
        itemDao = db.checklistItemDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun checklist削除で関連する全ChecklistItemがカスケード削除される() = runTest {
        val checklistId = "cl_1"
        checklistDao.insert(
            ChecklistEntity(id = checklistId, title = "旅行", createdAt = 1L, updatedAt = 1L)
        )

        val items = (1..5).map { i ->
            ChecklistItemEntity(
                id = "item_$i", checklistId = checklistId,
                name = "持ち物$i", sortOrder = i
            )
        }
        itemDao.insertAll(items)

        // 削除前: アイテムが存在する
        assertEquals(5, itemDao.getByChecklistId(checklistId).first().size)

        // Checklist削除
        checklistDao.deleteById(checklistId)

        // 削除後: Checklistが存在しない
        assertNull(checklistDao.getById(checklistId))
        // 削除後: 全関連アイテムがカスケード削除されている
        assertEquals(0, itemDao.getByChecklistId(checklistId).first().size)
    }

    @Test
    fun 他のChecklistのアイテムは影響を受けない() = runTest {
        checklistDao.insert(ChecklistEntity(id = "cl_a", title = "A", createdAt = 1L, updatedAt = 1L))
        checklistDao.insert(ChecklistEntity(id = "cl_b", title = "B", createdAt = 2L, updatedAt = 2L))

        itemDao.insertAll(listOf(
            ChecklistItemEntity(id = "a1", checklistId = "cl_a", name = "A1", sortOrder = 0),
            ChecklistItemEntity(id = "b1", checklistId = "cl_b", name = "B1", sortOrder = 0),
            ChecklistItemEntity(id = "b2", checklistId = "cl_b", name = "B2", sortOrder = 1)
        ))

        checklistDao.deleteById("cl_a")

        // cl_aのアイテムは削除
        assertEquals(0, itemDao.getByChecklistId("cl_a").first().size)
        // cl_bのアイテムは残存
        assertEquals(2, itemDao.getByChecklistId("cl_b").first().size)
    }
}
