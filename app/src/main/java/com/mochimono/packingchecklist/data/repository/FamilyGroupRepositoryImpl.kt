package com.mochimono.packingchecklist.data.repository

import com.mochimono.packingchecklist.data.local.dao.FamilyGroupDao
import com.mochimono.packingchecklist.data.local.entity.FamilyGroupEntity
import com.mochimono.packingchecklist.data.remote.AuthManager
import com.mochimono.packingchecklist.data.remote.FirestoreDataSource
import com.mochimono.packingchecklist.domain.model.FamilyGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyGroupRepositoryImpl @Inject constructor(
    private val authManager: AuthManager,
    private val firestoreDataSource: FirestoreDataSource,
    private val familyGroupDao: FamilyGroupDao
) : FamilyGroupRepository {

    override fun getCurrentGroup(): Flow<FamilyGroup?> =
        familyGroupDao.getCurrent().map { it?.let { FamilyGroup(it.id, it.inviteCode) } }

    override suspend fun createGroup(): String {
        val userId = authManager.ensureAuthenticated()
        val groupId = UUID.randomUUID().toString()
        val inviteCode = UUID.randomUUID().toString()

        firestoreDataSource.createFamilyGroup(groupId, inviteCode)
        firestoreDataSource.joinGroup(groupId, userId)
        familyGroupDao.insert(FamilyGroupEntity(groupId, inviteCode, System.currentTimeMillis()))

        return inviteCode
    }

    override suspend fun joinGroup(inviteCode: String) {
        val userId = authManager.ensureAuthenticated()
        val doc = firestoreDataSource.findGroupByInviteCode(inviteCode)
            ?: throw IllegalArgumentException("グループが見つかりません")

        firestoreDataSource.joinGroup(doc.id, userId)
        familyGroupDao.insert(
            FamilyGroupEntity(doc.id, inviteCode, System.currentTimeMillis())
        )
    }

    override suspend fun leaveGroup() {
        val userId = authManager.ensureAuthenticated()
        val group = familyGroupDao.getCurrent().firstOrNull() ?: return

        firestoreDataSource.leaveGroup(group.id, userId)
        familyGroupDao.deleteAll()
    }
}
