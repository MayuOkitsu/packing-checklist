package com.mochimono.packingchecklist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bags")
data class BagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ownerUserId: String? = null,
    val createdAt: Long
)
