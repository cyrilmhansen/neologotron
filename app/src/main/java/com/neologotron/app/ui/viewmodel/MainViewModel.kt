package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.domain.generator.GeneratorService
import com.neologotron.app.domain.GeneratorOptionsStore
import com.neologotron.app.data.repo.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val generator: GeneratorService,
    private val options: GeneratorOptionsStore,
    private val favorites: FavoriteRepository
) : ViewModel() {
    private val _word = MutableStateFlow("Néologisme")
    val word: StateFlow<String> = _word

    private val _definition = MutableStateFlow("Définition poétique ou technique apparaitra ici")
    val definition: StateFlow<String> = _definition

    private val _decomposition = MutableStateFlow("")
    val decomposition: StateFlow<String> = _decomposition

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _favoriteToggled = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val favoriteToggled: SharedFlow<Boolean> = _favoriteToggled

    val activeTags: StateFlow<Set<String>> = options.selectedTags

    fun generate(tags: Set<String> = options.selectedTags.value) {
        viewModelScope.launch {
            runCatching { generator.generateRandom(tags, saveToHistory = true) }
                .onSuccess {
                    _word.value = it.word
                    _definition.value = it.definition
                    _decomposition.value = it.decomposition
                    _isFavorite.value = favorites.isFavorited(it.word)
                }
        }
    }

    fun toggleFavorite() {
        val w = _word.value
        if (w.isBlank() || w == "Néologisme") return
        viewModelScope.launch {
            val currentlyFav = favorites.isFavorited(w)
            if (currentlyFav) {
                favorites.remove(w)
            } else {
                favorites.add(
                    word = w,
                    definition = _definition.value,
                    decomposition = _decomposition.value,
                    mode = "random"
                )
            }
            _isFavorite.value = !currentlyFav
            _favoriteToggled.emit(!currentlyFav)
        }
    }

    fun clearTags() { options.clear() }
}
