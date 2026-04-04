package com.mochimono.packingchecklist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_template_items",
    foreignKeys = [
        ForeignKey(
            entity = CustomTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId")]
)
data class CustomTemplateItemEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val name: String,
    val sortOrder: Int
)
