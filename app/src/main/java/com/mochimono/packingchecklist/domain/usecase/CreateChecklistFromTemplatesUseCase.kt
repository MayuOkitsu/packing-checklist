package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.data.repository.ChecklistRepository
import com.mochimono.packingchecklist.domain.model.Checklist
import com.mochimono.packingchecklist.domain.model.Result
import javax.inject.Inject

class CreateChecklistFromTemplatesUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val mergeTemplatesUseCase: MergeTemplatesUseCase
) {
    suspend operator fun invoke(title: String, customTemplateIds: List<String>): Result<Checklist> {
        if (title.isBlank()) {
            return Result.Error(IllegalArgumentException("タイトルを入力してください"))
        }

        val mergedItems = mergeTemplatesUseCase(customTemplateIds)
        val checklist = checklistRepository.createChecklist(title.trim())
        for (itemName in mergedItems) {
            checklistItemRepository.addItem(checklist.id, itemName)
        }
        return Result.Success(checklist)
    }
}
