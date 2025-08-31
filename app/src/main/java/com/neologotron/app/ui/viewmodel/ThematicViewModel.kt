package com.neologotron.app.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.repo.LexemeRepository
import com.neologotron.app.data.repo.SettingsRepository
import com.neologotron.app.domain.GeneratorOptionsStore
import com.neologotron.app.domain.generator.GeneratorService
import com.neologotron.app.ui.UiState

@HiltViewModel
class ThematicViewModel
    @Inject
    constructor(
        private val repo: LexemeRepository,
        private val options: GeneratorOptionsStore,
        private val generator: GeneratorService,
        private val settings: SettingsRepository,
    ) : ViewModel() {
        private val _tags = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
        val tags: StateFlow<UiState<List<String>>> = _tags

        private val _selected = MutableStateFlow<Set<String>>(emptySet())
        val selected: StateFlow<Set<String>> = _selected

        init {
            refreshTags()
        }

        fun refreshTags() {
            viewModelScope.launch {
                _tags.value = UiState.Loading
                val persisted = settings.selectedTags.first()
                _selected.value = persisted
                runCatching { repo.getDistinctTags() }
                    .onSuccess { list -> _tags.value = UiState.Data(list) }
                    .onFailure { err -> _tags.value = UiState.Error(err.message) }
            }
        }

        fun toggle(tag: String) {
            val newSel =
                _selected.value.toMutableSet().also { set ->
                    if (!set.add(tag)) set.remove(tag)
                }.toSet()
            _selected.value = newSel
            options.setTags(newSel)
            viewModelScope.launch { settings.setSelectedTags(newSel) }
        }

        fun reset() {
            _selected.value = emptySet()
            viewModelScope.launch { settings.setSelectedTags(emptySet()) }
            options.clear()
        }

        fun apply() {
            val sel = _selected.value
            options.setTags(sel)
            viewModelScope.launch { settings.setSelectedTags(sel) }
        }

        fun generateAndOpen(
            onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit,
        ) {
            viewModelScope.launch {
                val mode = settings.definitionMode.first()
                val filters = settings.coherenceFilters.first()
                val intensity = settings.weightingIntensity.first().toDouble()
                runCatching {
                    generator.generateRandom(
                        tags = _selected.value,
                        saveToHistory = true,
                        mode = mode,
                        useFilters = filters,
                        weightingIntensity = intensity,
                    )
                }
                    .onSuccess {
                        onOpenDetail(
                            it.word,
                            it.definition,
                            it.decomposition,
                            it.prefixForm,
                            it.rootForm,
                            it.suffixForm,
                            it.rootGloss,
                            it.rootConnectorPref,
                            it.suffixPosOut,
                            it.suffixDefTemplate,
                            it.suffixTags,
                            it.sources,
                        )
                    }
            }
        }
    }
