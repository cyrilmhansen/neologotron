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

    val selected: StateFlow<Set<String>> = options.selectedTags

    init {
        viewModelScope.launch {
            _tags.value = repo.getDistinctTags()
        }
    }

    fun toggle(tag: String) {
        val set = options.selectedTags.value.toMutableSet().also { s ->
            if (!s.add(tag)) s.remove(tag)
        }
        options.setTags(set)
    }

    fun reset() { options.clear() }

    fun generateAndOpen(onOpenDetail: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { generator.generateRandom(tags = selected.value, saveToHistory = true) }
                .onSuccess { onOpenDetail(it.word) }
        }
    }
}
