package com.mochimono.packingchecklist.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mochimono.packingchecklist.data.local.dao.BagDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistDao
import com.mochimono.packingchecklist.data.local.dao.ChecklistItemDao
import com.mochimono.packingchecklist.data.local.dao.CustomTemplateDao
import com.mochimono.packingchecklist.data.local.dao.CustomTemplateItemDao
import com.mochimono.packingchecklist.data.local.dao.DefaultTemplateItemDao
import com.mochimono.packingchecklist.data.local.dao.FamilyGroupDao
import com.mochimono.packingchecklist.data.local.dao.PendingSyncDao
import com.mochimono.packingchecklist.data.local.entity.BagEntity
import com.mochimono.packingchecklist.data.local.entity.ChecklistEntity
import com.mochimono.packingchecklist.data.local.entity.ChecklistItemEntity
import com.mochimono.packingchecklist.data.local.entity.CustomTemplateEntity
import com.mochimono.packingchecklist.data.local.entity.CustomTemplateItemEntity
import com.mochimono.packingchecklist.data.local.entity.DefaultTemplateItemEntity
import com.mochimono.packingchecklist.data.local.entity.FamilyGroupEntity
import com.mochimono.packingchecklist.data.local.entity.PendingSyncEntity

@Database(
    entities = [
        ChecklistEntity::class,
        ChecklistItemEntity::class,
        BagEntity::class,
        DefaultTemplateItemEntity::class,
        CustomTemplateEntity::class,
        CustomTemplateItemEntity::class,
        FamilyGroupEntity::class,
        PendingSyncEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PackingChecklistDatabase : RoomDatabase() {
    abstract fun checklistDao(): ChecklistDao
    abstract fun checklistItemDao(): ChecklistItemDao
    abstract fun bagDao(): BagDao
    abstract fun defaultTemplateItemDao(): DefaultTemplateItemDao
    abstract fun customTemplateDao(): CustomTemplateDao
    abstract fun customTemplateItemDao(): CustomTemplateItemDao
    abstract fun familyGroupDao(): FamilyGroupDao
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        const val DATABASE_NAME = "packing_checklist.db"

        private val DEFAULT_ITEMS = listOf(
            "パスポート", "充電器", "着替え", "歯ブラシ",
            "常備薬", "財布", "スマホ", "イヤホン"
        )

        fun createCallback(): Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DEFAULT_ITEMS.forEachIndexed { index, name ->
                    db.execSQL(
                        "INSERT INTO default_template_items (id, name, sortOrder) VALUES ('default_$index', ?, $index)",
                        arrayOf(name)
                    )
                }
            }
        }
    }
}
