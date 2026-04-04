package com.mochimono.packingchecklist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_groups")
data class FamilyGroupEntity(
    @PrimaryKey val id: String,
    val inviteCode: String,
    val createdAt: Long
)
