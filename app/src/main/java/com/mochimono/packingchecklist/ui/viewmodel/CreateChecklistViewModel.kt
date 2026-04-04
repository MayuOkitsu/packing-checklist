package com.mochimono.packingchecklist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochimono.packingchecklist.domain.model.Result
import com.mochimono.packingchecklist.domain.usecase.CreateChecklistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateChecklistState(
    val title: String = "",
    val errorMessage: String? = null,
    val createdChecklistId: String? = null
)

@HiltViewModel
class CreateChecklistViewModel @Inject constructor(
    private val createChecklistUseCase: CreateChecklistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChecklistState())
    val uiState: StateFlow<CreateChecklistState> = _uiState

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null)
    }

    fun create() {
        val title = _uiState.value.title
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "タイトルを入力してください")
            return
        }
        viewModelScope.launch {
            when (val result = createChecklistUseCase(title)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    createdChecklistId = result.data.id, errorMessage = null
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    errorMessage = result.exception.message
                )
            }
        }
    }
}
