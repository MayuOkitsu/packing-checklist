package com.mochimono.packingchecklist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSharingEnabled: Boolean = false,
    val familyGroupId: String? = null,
    val ownerUserId: String? = null
)
