package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.entity.HistoryEntity
import com.neologotron.app.data.repo.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: HistoryRepository
) : ViewModel() {
    private val _items = MutableStateFlow<List<HistoryEntity>>(emptyList())
    val items: StateFlow<List<HistoryEntity>> = _items

    init { refresh() }

    fun refresh() {
        viewModelScope.launch { _items.value = repo.recent() }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            runCatching { repo.delete(id) }
            _items.value = repo.recent()
        }
    }

    fun undoInsert(entity: HistoryEntity) {
        viewModelScope.launch {
            runCatching { repo.insert(entity.copy(id = 0)) }
            _items.value = repo.recent()
        }
    }
}
