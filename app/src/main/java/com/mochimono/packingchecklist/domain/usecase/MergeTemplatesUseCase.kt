package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.TemplateRepository
import com.mochimono.packingchecklist.domain.model.CustomTemplate
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MergeTemplatesUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    suspend operator fun invoke(customTemplateIds: List<String>): List<String> {
        val defaultItems = templateRepository.getDefaultTemplate().first().map { it.name }
        val customTemplates = templateRepository.getCustomTemplates().first()
        val selectedItems = customTemplates
            .filter { it.id in customTemplateIds }
            .flatMap { it.items }

        val seen = mutableSetOf<String>()
        return (defaultItems + selectedItems).filter { seen.add(it.trim()) }
    }
}
