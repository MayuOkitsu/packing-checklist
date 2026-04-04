package com.mochimono.packingchecklist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistEntity::class,
            parentColumns = ["id"],
            childColumns = ["checklistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("checklistId")]
)
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val checklistId: String,
    val name: String,
    val isChecked: Boolean = false,
    val bagId: String? = null,
    val lastModifiedAt: Long = 0L,
    val sortOrder: Int = 0
)
