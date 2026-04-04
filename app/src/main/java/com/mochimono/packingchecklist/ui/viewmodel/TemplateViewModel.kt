package com.mochimono.packingchecklist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochimono.packingchecklist.data.repository.TemplateRepository
import com.mochimono.packingchecklist.domain.model.CustomTemplate
import com.mochimono.packingchecklist.domain.model.Result
import com.mochimono.packingchecklist.domain.model.TemplateItem
import com.mochimono.packingchecklist.domain.model.UiState
import com.mochimono.packingchecklist.domain.usecase.CreateChecklistFromTemplatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemplateState(
    val defaultItems: List<TemplateItem> = emptyList(),
    val customTemplates: List<CustomTemplate> = emptyList(),
    val selectedTemplateIds: Set<String> = emptySet(),
    val createdChecklistId: String? = null
)

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val createFromTemplatesUseCase: CreateChecklistFromTemplatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState<TemplateState>(isLoading = true))
    val uiState: StateFlow<UiState<TemplateState>> = _uiState

    init {
        viewModelScope.launch {
            combine(
                templateRepository.getDefaultTemplate(),
                templateRepository.getCustomTemplates()
            ) { defaults, customs -> TemplateState(defaultItems = defaults, customTemplates = customs) }
                .catch { e -> _uiState.value = UiState(error = e.message) }
                .collect { state ->
                    _uiState.value = UiState(
                        data = state.copy(selectedTemplateIds = _uiState.value.data?.selectedTemplateIds ?: emptySet())
                    )
                }
        }
    }

    // Default Template操作
    fun addDefaultItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val current = _uiState.value.data?.defaultItems ?: return@launch
            val newItem = TemplateItem(
                id = java.util.UUID.randomUUID().toString(),
                name = name.trim(),
                sortOrder = current.size
            )
            templateRepository.updateDefaultTemplate(current + newItem)
        }
    }

    fun removeDefaultItem(id: String) {
        viewModelScope.launch {
            val current = _uiState.value.data?.defaultItems ?: return@launch
            templateRepository.updateDefaultTemplate(current.filter { it.id != id })
        }
    }

    // Custom Template操作
    fun updateCustomTemplateLabel(id: String, label: String) {
        if (label.isBlank()) return
        viewModelScope.launch {
            val template = _uiState.value.data?.customTemplates?.find { it.id == id } ?: return@launch
            templateRepository.updateCustomTemplate(id, label.trim(), template.items)
        }
    }

    fun updateCustomTemplateItems(id: String, items: List<String>) {
        viewModelScope.launch {
            val template = _uiState.value.data?.customTemplates?.find { it.id == id } ?: return@launch
            templateRepository.updateCustomTemplate(id, template.label, items)
        }
    }

    fun deleteCustomTemplate(id: String) {
        viewModelScope.launch { templateRepository.deleteCustomTemplate(id) }
    }

    // テンプレート選択（複数選択対応）
    fun toggleTemplateSelection(id: String) {
        val current = _uiState.value.data ?: return
        val updated = if (id in current.selectedTemplateIds)
            current.selectedTemplateIds - id else current.selectedTemplateIds + id
        _uiState.value = _uiState.value.copy(data = current.copy(selectedTemplateIds = updated))
    }

    fun createFromSelectedTemplates(title: String) {
        val selected = _uiState.value.data?.selectedTemplateIds?.toList() ?: return
        if (selected.isEmpty()) return
        viewModelScope.launch {
            when (val result = createFromTemplatesUseCase(title, selected)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    data = _uiState.value.data?.copy(
                        createdChecklistId = result.data.id,
                        selectedTemplateIds = emptySet()
                    )
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
        }
    }
}
