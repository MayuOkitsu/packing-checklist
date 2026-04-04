package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.BagRepository
import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import javax.inject.Inject

class DeleteBagUseCase @Inject constructor(
    private val bagRepository: BagRepository,
    private val checklistItemRepository: ChecklistItemRepository
) {
    suspend operator fun invoke(bagId: String) {
        checklistItemRepository.clearBagAssignments(bagId)
        bagRepository.deleteBag(bagId)
    }
}
