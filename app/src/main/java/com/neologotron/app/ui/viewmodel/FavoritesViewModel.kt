package com.neologotron.app.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.entity.FavoriteEntity
import com.neologotron.app.data.repo.FavoriteRepository
import com.neologotron.app.ui.UiState

@HiltViewModel
class FavoritesViewModel
    @Inject
    constructor(
        private val repo: FavoriteRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow<UiState<List<FavoriteEntity>>>(UiState.Loading)
        val state: StateFlow<UiState<List<FavoriteEntity>>> = _state

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                _state.value = UiState.Loading
                runCatching { repo.list() }
                    .onSuccess { _state.value = UiState.Data(it) }
                    .onFailure { _state.value = UiState.Error(it.message) }
            }
        }

        fun remove(id: Long) {
            viewModelScope.launch {
                runCatching { repo.removeById(id) }
                _state.value = UiState.Data(repo.list())
            }
        }

        fun undoInsert(entity: FavoriteEntity) {
            viewModelScope.launch {
                runCatching { repo.insert(entity.copy(id = 0)) }
                _state.value = UiState.Data(repo.list())
            }
        }
    }
