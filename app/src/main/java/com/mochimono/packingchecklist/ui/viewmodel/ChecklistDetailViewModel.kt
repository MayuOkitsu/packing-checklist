package com.mochimono.packingchecklist.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.data.repository.ChecklistRepository
import com.mochimono.packingchecklist.domain.model.Bag
import com.mochimono.packingchecklist.domain.model.ChecklistItem
import com.mochimono.packingchecklist.domain.model.BagSuggestion
import com.mochimono.packingchecklist.domain.model.Result
import com.mochimono.packingchecklist.domain.model.UiState
import com.mochimono.packingchecklist.domain.usecase.GetBagSuggestionsUseCase
import com.mochimono.packingchecklist.domain.usecase.SaveAsCustomTemplateUseCase
import com.mochimono.packingchecklist.domain.usecase.ToggleCheckItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BagGroup(val bag: Bag?, val items: List<ChecklistItem>)

data class ChecklistDetailState(
    val checklistId: String = "",
    val title: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val isSharingEnabled: Boolean = false,
    val isGroupedView: Boolean = false
) {
    val checkedCount: Int get() = items.count { it.isChecked }
    val totalCount: Int get() = items.size
    val progress: String get() = "$checkedCount/$totalCount"

    val groupedItems: List<BagGroup> get() {
        val byBag = items.groupBy { it.bag }
        val assigned = byBag.filterKeys { it != null }
            .map { (bag, items) -> BagGroup(bag, items) }
        val unassigned = byBag[null]?.let { listOf(BagGroup(null, it)) } ?: emptyList()
        return assigned + unassigned
    }
}

@HiltViewModel
class ChecklistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val toggleCheckItemUseCase: ToggleCheckItemUseCase,
    private val saveAsCustomTemplateUseCase: SaveAsCustomTemplateUseCase,
    private val getBagSuggestionsUseCase: GetBagSuggestionsUseCase
) : ViewModel() {

    private val checklistId: String = savedStateHandle["checklistId"] ?: ""

    private val _uiState = MutableStateFlow(UiState<ChecklistDetailState>(isLoading = true))
    val uiState: StateFlow<UiState<ChecklistDetailState>> = _uiState

    private val _lastAssignedBagId = MutableStateFlow<String?>(null)
    val lastAssignedBagId: StateFlow<String?> = _lastAssignedBagId

    init {
        loadChecklist()
        observeItems()
    }

    private fun loadChecklist() {
        viewModelScope.launch {
            val checklist = checklistRepository.getChecklistById(checklistId)
            if (checklist != null) {
                _uiState.value = UiState(
                    data = ChecklistDetailState(
                        checklistId = checklist.id,
                        title = checklist.title,
                        isSharingEnabled = checklist.isSharingEnabled
                    )
                )
            } else {
                _uiState.value = UiState(error = "チェックリストが見つかりません")
            }
        }
    }

    private fun observeItems() {
        viewModelScope.launch {
            checklistItemRepository.getItemsByChecklistId(checklistId)
                .catch { e -> _uiState.value = UiState(error = e.message) }
                .collect { items ->
                    _uiState.value = _uiState.value.copy(
                        data = _uiState.value.data?.copy(items = items)
                    )
                }
        }
    }

    fun toggleCheck(item: ChecklistItem, bagId: String?) {
        viewModelScope.launch {
            toggleCheckItemUseCase(item, bagId)
            if (!item.isChecked && bagId != null) {
                _lastAssignedBagId.value = bagId
            }
        }
    }

    fun addItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            checklistItemRepository.addItem(checklistId, name.trim())
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch { checklistItemRepository.deleteItem(id) }
    }

    fun updateItemName(id: String, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { checklistItemRepository.updateItemName(id, name.trim()) }
    }

    fun updateTitle(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            checklistRepository.updateChecklistTitle(checklistId, title.trim())
            _uiState.value = _uiState.value.copy(
                data = _uiState.value.data?.copy(title = title.trim())
            )
        }
    }

    fun toggleGroupedView() {
        _uiState.value = _uiState.value.copy(
            data = _uiState.value.data?.copy(
                isGroupedView = !(_uiState.value.data?.isGroupedView ?: false)
            )
        )
    }

    fun saveAsTemplate(label: String, selectedItemNames: List<String>) {
        viewModelScope.launch {
            when (val result = saveAsCustomTemplateUseCase(label, selectedItemNames)) {
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.exception.message)
                is Result.Success -> {} // テンプレート保存成功
            }
        }
    }

    fun toggleSharing() {
        viewModelScope.launch {
            val current = _uiState.value.data?.isSharingEnabled ?: return@launch
            checklistRepository.setSharingEnabled(checklistId, !current)
            _uiState.value = _uiState.value.copy(
                data = _uiState.value.data?.copy(isSharingEnabled = !current)
            )
        }
    }

    suspend fun getBagSuggestions(): List<BagSuggestion> {
        return getBagSuggestionsUseCase(checklistId, _lastAssignedBagId.value)
    }
}
