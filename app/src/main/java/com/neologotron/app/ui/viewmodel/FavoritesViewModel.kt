package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.entity.FavoriteEntity
import com.neologotron.app.data.repo.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repo: FavoriteRepository
) : ViewModel() {
    private val _items = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val items: StateFlow<List<FavoriteEntity>> = _items

    init { refresh() }

    fun refresh() {
        viewModelScope.launch { _items.value = repo.list() }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            runCatching { repo.removeById(id) }
            _items.value = repo.list()
        }
    }

    fun undoInsert(entity: FavoriteEntity) {
        viewModelScope.launch {
            runCatching { repo.insert(entity.copy(id = 0)) }
            _items.value = repo.list()
        }
    }
}
