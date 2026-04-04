package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.BagRepository
import com.mochimono.packingchecklist.data.repository.FamilyGroupRepository
import com.mochimono.packingchecklist.data.repository.SyncManager
import com.mochimono.packingchecklist.data.repository.TemplateRepository
import com.mochimono.packingchecklist.data.remote.FirestoreDataSource
import com.mochimono.packingchecklist.data.remote.AuthManager
import com.mochimono.packingchecklist.domain.model.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ManageFamilyGroupUseCase @Inject constructor(
    private val familyGroupRepository: FamilyGroupRepository,
    private val bagRepository: BagRepository,
    private val templateRepository: TemplateRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val authManager: AuthManager,
    private val syncManager: SyncManager
) {
    suspend fun createGroup(): Result<String> = try {
        val inviteCode = familyGroupRepository.createGroup()
        val group = familyGroupRepository.getCurrentGroup().firstOrNull()
        if (group != null) {
            syncBagsAndTemplates(group.id)
        }
        Result.Success(inviteCode)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun joinGroup(inviteCode: String): Result<Unit> = try {
        familyGroupRepository.joinGroup(inviteCode)
        val group = familyGroupRepository.getCurrentGroup().firstOrNull()
            ?: return Result.Error(IllegalStateException("グループ参加に失敗しました"))
        syncBagsAndTemplates(group.id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun leaveGroup(): Result<Unit> = try {
        familyGroupRepository.leaveGroup()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    private suspend fun syncBagsAndTemplates(groupId: String) {
        val userId = authManager.ensureAuthenticated()

        bagRepository.getAllBags().first().forEach { bag ->
            firestoreDataSource.setBag(
                groupId, bag.id,
                mapOf("name" to bag.name, "ownerUserId" to userId)
            )
        }

        templateRepository.getCustomTemplates().first().forEach { template ->
            firestoreDataSource.setTemplate(
                groupId, template.id,
                mapOf(
                    "label" to template.label,
                    "ownerUserId" to userId,
                    "items" to template.items.mapIndexed { i, name ->
                        mapOf("name" to name, "sortOrder" to i)
                    }
                )
            )
        }
    }
}
