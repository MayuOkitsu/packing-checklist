package com.mochimono.packingchecklist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "default_template_items")
data class DefaultTemplateItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int
)
