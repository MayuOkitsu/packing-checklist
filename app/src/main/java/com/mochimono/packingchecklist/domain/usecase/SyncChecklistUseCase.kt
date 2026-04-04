package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.ChecklistRepository
import com.mochimono.packingchecklist.data.repository.FamilyGroupRepository
import com.mochimono.packingchecklist.data.repository.SyncManager
import com.mochimono.packingchecklist.domain.model.Result
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SyncChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val familyGroupRepository: FamilyGroupRepository,
    private val syncManager: SyncManager
) {
    suspend operator fun invoke(checklistId: String): Result<Unit> = try {
        val checklist = checklistRepository.getChecklistById(checklistId)
            ?: return Result.Error(IllegalArgumentException("チェックリストが見つかりません"))

        if (!checklist.isSharingEnabled) {
            return Result.Success(Unit) // 非共有はローカルのみ、同期不要
        }

        val group = familyGroupRepository.getCurrentGroup().firstOrNull()
            ?: return Result.Error(IllegalStateException("Family Groupに参加していません"))

        syncManager.processPendingSyncs(group.id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
