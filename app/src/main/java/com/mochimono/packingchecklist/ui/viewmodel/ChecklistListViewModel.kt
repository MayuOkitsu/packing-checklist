package com.mochimono.packingchecklist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochimono.packingchecklist.data.repository.ChecklistRepository
import com.mochimono.packingchecklist.domain.model.Checklist
import com.mochimono.packingchecklist.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecklistListViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState<List<Checklist>>(isLoading = true))
    val uiState: StateFlow<UiState<List<Checklist>>> = _uiState

    init {
        viewModelScope.launch {
            checklistRepository.getAllChecklists()
                .catch { e -> _uiState.value = UiState(error = e.message) }
                .collect { checklists ->
                    _uiState.value = UiState(data = checklists)
                }
        }
    }

    fun deleteChecklist(id: String) {
        viewModelScope.launch {
            try {
                checklistRepository.deleteChecklist(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
