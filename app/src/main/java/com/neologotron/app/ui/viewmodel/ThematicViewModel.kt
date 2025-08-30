package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.repo.LexemeRepository
import com.neologotron.app.domain.generator.GeneratorService
import dagger.hilt.android.lifecycle.HiltViewModel
import com.neologotron.app.domain.GeneratorOptionsStore
import com.neologotron.app.data.repo.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class ThematicViewModel @Inject constructor(
    private val repo: LexemeRepository,
    private val options: GeneratorOptionsStore,
    private val generator: GeneratorService,
    private val settings: SettingsRepository
) : ViewModel() {
    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected

    init {
        viewModelScope.launch {
            _tags.value = repo.getDistinctTags()
            // Load persisted selection and reflect it locally
            val persisted = settings.selectedTags.first()
            _selected.value = persisted
        }
    }

    fun toggle(tag: String) {
        val newSel = _selected.value.toMutableSet().also { set ->
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

    fun generateAndOpen(onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit) {
        viewModelScope.launch {
            val mode = settings.definitionMode.first()
            val filters = settings.coherenceFilters.first()
            runCatching { generator.generateRandom(tags = _selected.value, saveToHistory = true, mode = mode, useFilters = filters) }
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
                    )
                }
        }
    }
}
