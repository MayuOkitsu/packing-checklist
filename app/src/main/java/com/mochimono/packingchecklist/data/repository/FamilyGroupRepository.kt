package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.domain.model.FamilyGroup
import kotlinx.coroutines.flow.Flow

interface FamilyGroupRepository {
    fun getCurrentGroup(): Flow<FamilyGroup?>
    suspend fun createGroup(): String
    suspend fun joinGroup(inviteCode: String)
    suspend fun leaveGroup()
}
