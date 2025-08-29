package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.repo.LexemeRepository
import com.neologotron.app.domain.generator.GeneratorService
import dagger.hilt.android.lifecycle.HiltViewModel
import com.neologotron.app.domain.GeneratorOptionsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThematicViewModel @Inject constructor(
    private val repo: LexemeRepository,
    private val options: GeneratorOptionsStore,
    private val generator: GeneratorService
) : ViewModel() {
    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected

    init {
        viewModelScope.launch {
            _tags.value = repo.getDistinctTags()
        }
    }

    fun toggle(tag: String) {
        _selected.value = _selected.value.toMutableSet().also { set ->
            if (!set.add(tag)) set.remove(tag)
        }
    }

    fun reset() {
        _selected.value = emptySet()
    }

    fun apply() { options.setTags(_selected.value) }

    fun generateAndOpen(onOpenDetail: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { generator.generateRandom(tags = _selected.value, saveToHistory = true) }
                .onSuccess { onOpenDetail(it.word) }
        }
    }
}
