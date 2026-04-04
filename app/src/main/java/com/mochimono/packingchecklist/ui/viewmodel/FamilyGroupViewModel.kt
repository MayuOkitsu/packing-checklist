package com.mochimono.packingchecklist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochimono.packingchecklist.domain.model.FamilyGroup
import com.mochimono.packingchecklist.domain.model.Result
import com.mochimono.packingchecklist.domain.model.UiState
import com.mochimono.packingchecklist.domain.usecase.ManageFamilyGroupUseCase
import com.mochimono.packingchecklist.data.repository.FamilyGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyGroupState(
    val group: FamilyGroup? = null,
    val inviteCodeInput: String = "",
    val codeCopied: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class FamilyGroupViewModel @Inject constructor(
    private val manageFamilyGroupUseCase: ManageFamilyGroupUseCase,
    private val familyGroupRepository: FamilyGroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState<FamilyGroupState>(isLoading = true))
    val uiState: StateFlow<UiState<FamilyGroupState>> = _uiState

    init {
        viewModelScope.launch {
            familyGroupRepository.getCurrentGroup()
                .catch { e -> _uiState.value = UiState(error = e.message) }
                .collect { group ->
                    _uiState.value = UiState(
                        data = (_uiState.value.data ?: FamilyGroupState()).copy(group = group)
                    )
                }
        }
    }

    fun createGroup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(data = _uiState.value.data?.copy(message = null))
            when (val result = manageFamilyGroupUseCase.createGroup()) {
                is Result.Success -> {} // Flowが自動更新
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
        }
    }

    fun updateInviteCodeInput(code: String) {
        _uiState.value = _uiState.value.copy(
            data = _uiState.value.data?.copy(inviteCodeInput = code)
        )
    }

    fun joinGroup() {
        val code = _uiState.value.data?.inviteCodeInput?.trim() ?: return
        if (code.isBlank()) return
        viewModelScope.launch {
            when (val result = manageFamilyGroupUseCase.joinGroup(code)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    data = _uiState.value.data?.copy(inviteCodeInput = "", message = "グループに参加しました")
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
        }
    }

    fun leaveGroup() {
        viewModelScope.launch {
            when (val result = manageFamilyGroupUseCase.leaveGroup()) {
                is Result.Success -> {} // Flowが自動更新
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.exception.message)
            }
        }
    }

    fun onCodeCopied() {
        _uiState.value = _uiState.value.copy(
            data = _uiState.value.data?.copy(codeCopied = true)
        )
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(
                data = _uiState.value.data?.copy(codeCopied = false)
            )
        }
    }

    fun getInviteCode(): String? = _uiState.value.data?.group?.inviteCode
}
