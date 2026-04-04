package com.mochimono.packingchecklist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochimono.packingchecklist.data.repository.BagRepository
import com.mochimono.packingchecklist.domain.model.Bag
import com.mochimono.packingchecklist.domain.model.UiState
import com.mochimono.packingchecklist.domain.usecase.DeleteBagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BagState(
    val bags: List<Bag> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BagViewModel @Inject constructor(
    private val bagRepository: BagRepository,
    private val deleteBagUseCase: DeleteBagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState<BagState>(isLoading = true))
    val uiState: StateFlow<UiState<BagState>> = _uiState

    init {
        viewModelScope.launch {
            bagRepository.getAllBags()
                .catch { e -> _uiState.value = UiState(error = e.message) }
                .collect { bags ->
                    _uiState.value = UiState(data = BagState(bags = bags))
                }
        }
    }

    fun addBag(name: String) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                data = _uiState.value.data?.copy(errorMessage = "鞄の名前を入力してください")
            )
            return
        }
        viewModelScope.launch {
            bagRepository.createBag(name.trim())
            _uiState.value = _uiState.value.copy(
                data = _uiState.value.data?.copy(errorMessage = null)
            )
        }
    }

    fun updateBagName(id: String, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { bagRepository.updateBagName(id, name.trim()) }
    }

    fun deleteBag(id: String) {
        viewModelScope.launch { deleteBagUseCase(id) }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            data = _uiState.value.data?.copy(errorMessage = null)
        )
    }
}
