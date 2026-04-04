package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.TemplateRepository
import com.mochimono.packingchecklist.domain.model.CustomTemplate
import com.mochimono.packingchecklist.domain.model.Result
import javax.inject.Inject

class SaveAsCustomTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    suspend operator fun invoke(label: String, selectedItemNames: List<String>): Result<CustomTemplate> {
        if (label.isBlank()) {
            return Result.Error(IllegalArgumentException("ラベルを入力してください"))
        }
        if (selectedItemNames.isEmpty()) {
            return Result.Error(IllegalArgumentException("項目を選択してください"))
        }

        val template = templateRepository.saveAsCustomTemplate(label.trim(), selectedItemNames)
        return Result.Success(template)
    }
}
