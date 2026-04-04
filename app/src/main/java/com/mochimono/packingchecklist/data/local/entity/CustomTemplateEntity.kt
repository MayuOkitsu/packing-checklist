package com.mochimono.packingchecklist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_templates")
data class CustomTemplateEntity(
    @PrimaryKey val id: String,
    val label: String,
    val createdAt: Long,
    val updatedAt: Long,
    val ownerUserId: String? = null
)
