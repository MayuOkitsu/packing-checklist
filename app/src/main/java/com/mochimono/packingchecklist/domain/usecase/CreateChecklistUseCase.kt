package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.data.repository.ChecklistRepository
import com.mochimono.packingchecklist.data.repository.TemplateRepository
import com.mochimono.packingchecklist.domain.model.Checklist
import com.mochimono.packingchecklist.domain.model.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val templateRepository: TemplateRepository
) {
    suspend operator fun invoke(title: String): Result<Checklist> {
        if (title.isBlank()) {
            return Result.Error(IllegalArgumentException("タイトルを入力してください"))
        }

        val checklist = checklistRepository.createChecklist(title.trim())
        val defaultItems = templateRepository.getDefaultTemplate().first()
        for (item in defaultItems) {
            checklistItemRepository.addItem(checklist.id, item.name)
        }
        return Result.Success(checklist)
    }
}
