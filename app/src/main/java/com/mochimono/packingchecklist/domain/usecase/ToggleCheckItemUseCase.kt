package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.domain.model.ChecklistItem
import javax.inject.Inject

class ToggleCheckItemUseCase @Inject constructor(
    private val checklistItemRepository: ChecklistItemRepository
) {
    suspend operator fun invoke(item: ChecklistItem, bagId: String? = null): ChecklistItem {
        return if (item.isChecked) {
            checklistItemRepository.uncheckItem(item.id)
        } else {
            checklistItemRepository.toggleCheck(item.id, bagId)
        }
    }
}
