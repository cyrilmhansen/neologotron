package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.domain.generator.GeneratorService
import com.neologotron.app.domain.GeneratorOptionsStore
import com.neologotron.app.data.repo.FavoriteRepository
import com.neologotron.app.data.repo.SettingsRepository
import com.neologotron.app.domain.generator.GeneratorRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val generator: GeneratorService,
    private val options: GeneratorOptionsStore,
    private val favorites: FavoriteRepository,
    private val settings: SettingsRepository
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

    val definitionMode = settings.definitionMode
    val shakeEnabled = settings.shakeToGenerate
    val hapticOnShake = settings.hapticOnShake
    val shakeHintShown = settings.shakeHintShown

    fun markShakeHintShown() { viewModelScope.launch { settings.setShakeHintShown(true) } }

    init {
        // Keep in-memory tag options in sync with persisted selection
        viewModelScope.launch {
            settings.selectedTags.collect { tags -> options.setTags(tags) }
        }
    }

    fun generate(tags: Set<String> = options.selectedTags.value) {
        viewModelScope.launch {
            val mode = definitionMode.first()
            val filters = settings.coherenceFilters.first()
            runCatching { generator.generateRandom(tags, saveToHistory = true, mode = mode, useFilters = filters) }
                .onSuccess {
                    _word.value = it.word
                    _definition.value = it.definition
                    _decomposition.value = it.decomposition
                    _isFavorite.value = favorites.isFavorited(it.word)
                }
        }
    }

    fun setDefinitionMode(mode: GeneratorRules.DefinitionMode) {
        viewModelScope.launch { settings.setDefinitionMode(mode) }
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
}
