package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.entity.HistoryEntity
import com.neologotron.app.data.repo.HistoryRepository
import com.neologotron.app.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: HistoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<HistoryEntity>>>(UiState.Loading)
    val state: StateFlow<UiState<List<HistoryEntity>>> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching { repo.recent() }
                .onSuccess { _state.value = UiState.Data(it) }
                .onFailure { _state.value = UiState.Error(it.message) }
        }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            runCatching { repo.delete(id) }
            _state.value = UiState.Data(repo.recent())
        }
    }

    fun undoInsert(entity: HistoryEntity) {
        viewModelScope.launch {
            runCatching { repo.insert(entity.copy(id = 0)) }
            _state.value = UiState.Data(repo.recent())
        }
    }
}
