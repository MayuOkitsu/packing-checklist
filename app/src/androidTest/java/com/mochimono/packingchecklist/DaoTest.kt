package com.mochimono.packingchecklist

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mochimono.packingchecklist.data.local.dao.BagDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.db.PackingChecklistDatabase
import com.mochimono.packingchecklist.data.local.entity.BagEntity
import com.mochimono.packingchecklist.data.local.entity.ChecklistEntity
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: PackingChecklistDatabase
    private lateinit var checklistDao: ChecklistDao
    private lateinit var itemDao: ChecklistItemDao
    private lateinit var bagDao: BagDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, PackingChecklistDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        checklistDao = db.checklistDao()
        itemDao = db.checklistItemDao()
        bagDao = db.bagDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ========== ChecklistDao ==========

    @Test
    fun checklistDao_insertAndGetById() = runTest {
        val entity = checklist("1", "旅行")
        checklistDao.insert(entity)
        val result = checklistDao.getById("1")
        assertNotNull(result)
        assertEquals("旅行", result!!.title)
    }

    @Test
    fun checklistDao_update() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        checklistDao.updateTitle("1", "出張", 2000L)
        assertEquals("出張", checklistDao.getById("1")!!.title)
    }

    @Test
    fun checklistDao_delete() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        checklistDao.deleteById("1")
        assertNull(checklistDao.getById("1"))
    }

    @Test
    fun checklistDao_getAllOrderedByCreatedAtDesc() = runTest {
        checklistDao.insert(checklist("1", "古い", createdAt = 1000L))
        checklistDao.insert(checklist("2", "新しい", createdAt = 3000L))
        checklistDao.insert(checklist("3", "中間", createdAt = 2000L))
        val result = checklistDao.getAll().first()
        assertEquals(listOf("新しい", "中間", "古い"), result.map { it.title })
    }

    @Test
    fun checklistDao_getAllWithProgress() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        itemDao.insert(item("i1", "1", isChecked = true))
        itemDao.insert(item("i2", "1", isChecked = false))
        itemDao.insert(item("i3", "1", isChecked = true))

        val result = checklistDao.getAllWithProgress().first()
        assertEquals(1, result.size)
        assertEquals(2, result[0].checkedCount)
        assertEquals(3, result[0].totalCount)
    }

    @Test
    fun checklistDao_getAllWithProgress_noItems() = runTest {
        checklistDao.insert(checklist("1", "空"))
        val result = checklistDao.getAllWithProgress().first()
        assertEquals(0, result[0].checkedCount)
        assertEquals(0, result[0].totalCount)
    }

    // ========== ChecklistItemDao ==========

    @Test
    fun itemDao_insertAndGetById() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        val entity = item("i1", "1", name = "パスポート")
        itemDao.insert(entity)
        val result = itemDao.getById("i1")
        assertNotNull(result)
        assertEquals("パスポート", result!!.name)
    }

    @Test
    fun itemDao_update() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        itemDao.insert(item("i1", "1", name = "パスポート"))
        itemDao.updateName("i1", "充電器", 2000L)
        assertEquals("充電器", itemDao.getById("i1")!!.name)
    }

    @Test
    fun itemDao_delete() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        itemDao.insert(item("i1", "1"))
        itemDao.deleteById("i1")
        assertNull(itemDao.getById("i1"))
    }

    @Test
    fun itemDao_cascadeDeleteOnChecklistRemoval() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        itemDao.insert(item("i1", "1"))
        itemDao.insert(item("i2", "1"))
        checklistDao.deleteById("1")
        assertNull(itemDao.getById("i1"))
        assertNull(itemDao.getById("i2"))
    }

    @Test
    fun itemDao_clearBagAssignment() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        itemDao.insert(item("i1", "1", bagId = "b1"))
        itemDao.insert(item("i2", "1", bagId = "b1"))
        itemDao.insert(item("i3", "1", bagId = "b2"))
        itemDao.clearBagAssignment("b1")
        assertNull(itemDao.getById("i1")!!.bagId)
        assertNull(itemDao.getById("i2")!!.bagId)
        assertEquals("b2", itemDao.getById("i3")!!.bagId)
    }

    @Test
    fun itemDao_getByChecklistIdWithBag_joinsBagData() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        bagDao.insert(BagEntity("b1", "リュック", null, 1000L))
        itemDao.insert(item("i1", "1", bagId = "b1"))
        itemDao.insert(item("i2", "1", bagId = null))

        val result = itemDao.getByChecklistIdWithBag("1").first()
        assertEquals(2, result.size)
        val withBag = result.find { it.id == "i1" }!!
        assertEquals("b1", withBag.bagJoinId)
        assertEquals("リュック", withBag.bagJoinName)
        val noBag = result.find { it.id == "i2" }!!
        assertNull(noBag.bagJoinId)
    }

    @Test
    fun itemDao_flowEmitsOnChange() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        val initial = itemDao.getByChecklistId("1").first()
        assertTrue(initial.isEmpty())

        itemDao.insert(item("i1", "1"))
        val afterInsert = itemDao.getByChecklistId("1").first()
        assertEquals(1, afterInsert.size)
    }

    @Test
    fun itemDao_updateCheckState() = runTest {
        checklistDao.insert(checklist("1", "旅行"))
        itemDao.insert(item("i1", "1"))
        itemDao.updateCheckState("i1", isChecked = true, bagId = "b1", lastModifiedAt = 5000L)
        val result = itemDao.getById("i1")!!
        assertTrue(result.isChecked)
        assertEquals("b1", result.bagId)
    }

    // ========== BagDao ==========

    @Test
    fun bagDao_insertAndGetById() = runTest {
        bagDao.insert(BagEntity("b1", "リュック", null, 1000L))
        val result = bagDao.getById("b1")
        assertNotNull(result)
        assertEquals("リュック", result!!.name)
    }

    @Test
    fun bagDao_update() = runTest {
        val entity = BagEntity("b1", "リュック", null, 1000L)
        bagDao.insert(entity)
        bagDao.update(entity.copy(name = "スーツケース"))
        assertEquals("スーツケース", bagDao.getById("b1")!!.name)
    }

    @Test
    fun bagDao_delete() = runTest {
        bagDao.insert(BagEntity("b1", "リュック", null, 1000L))
        bagDao.deleteById("b1")
        assertNull(bagDao.getById("b1"))
    }

    @Test
    fun bagDao_getAllFlowEmission() = runTest {
        val initial = bagDao.getAll().first()
        assertTrue(initial.isEmpty())

        bagDao.insert(BagEntity("b1", "リュック", null, 1000L))
        val afterInsert = bagDao.getAll().first()
        assertEquals(1, afterInsert.size)
    }

    // ========== Helpers ==========

    private fun checklist(id: String, title: String, createdAt: Long = 1000L) =
        ChecklistEntity(id, title, createdAt, createdAt)

    private fun item(
        id: String, checklistId: String, name: String = "item",
        isChecked: Boolean = false, bagId: String? = null, sortOrder: Int = 0
    ) = ChecklistItemEntity(id, checklistId, name, isChecked, bagId, 0L, sortOrder)
}
