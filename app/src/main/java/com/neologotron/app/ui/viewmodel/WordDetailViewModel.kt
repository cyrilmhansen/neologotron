package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.repo.FavoriteRepository
import com.neologotron.app.data.repo.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val history: HistoryRepository,
    private val favorites: FavoriteRepository
) : ViewModel() {

    private val wordArg: String = savedStateHandle.get<String>("word").orEmpty()
    private val defArg: String = savedStateHandle.get<String>("def").orEmpty()
    private val decompArg: String = savedStateHandle.get<String>("decomp").orEmpty()

    private val _word = MutableStateFlow(wordArg)
    val word: StateFlow<String> = _word

    private val _definition = MutableStateFlow("")
    val definition: StateFlow<String> = _definition

    private val _decomposition = MutableStateFlow("")
    val decomposition: StateFlow<String> = _decomposition

    private val _mode = MutableStateFlow("random")
    val mode: StateFlow<String> = _mode

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _favoriteToggled = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val favoriteToggled: SharedFlow<Boolean> = _favoriteToggled

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val latest = history.latestByWord(wordArg)
            if (latest != null) {
                _definition.value = latest.definition
                _decomposition.value = latest.decomposition
                _mode.value = latest.mode
            } else {
                val fav = favorites.get(wordArg)
                if (fav != null) {
                    _definition.value = fav.definition
                    _decomposition.value = fav.decomposition
                    _mode.value = fav.mode
                } else {
                    // Fallback to navigation-provided preview, if any
                    if (defArg.isNotBlank()) _definition.value = defArg
                    if (decompArg.isNotBlank()) _decomposition.value = decompArg
                    if (defArg.isNotBlank() || decompArg.isNotBlank()) _mode.value = "preview"
                }
            }
            _isFavorite.value = favorites.isFavorited(wordArg)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentlyFav = favorites.isFavorited(wordArg)
            if (currentlyFav) {
                favorites.remove(wordArg)
            } else {
                favorites.add(
                    word = wordArg,
                    definition = _definition.value.ifBlank { "" },
                    decomposition = _decomposition.value.ifBlank { "" },
                    mode = _mode.value
                )
            }
            _isFavorite.value = !currentlyFav
            _favoriteToggled.emit(!currentlyFav)
        }
    }
}
