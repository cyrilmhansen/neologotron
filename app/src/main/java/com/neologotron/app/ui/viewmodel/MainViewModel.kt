package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.domain.generator.GeneratorService
import com.neologotron.app.domain.GeneratorOptionsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val generator: GeneratorService,
    private val options: GeneratorOptionsStore
) : ViewModel() {
    private val _word = MutableStateFlow("Néologisme")
    val word: StateFlow<String> = _word

    private val _definition = MutableStateFlow("Définition poétique ou technique apparaitra ici")
    val definition: StateFlow<String> = _definition

    fun generate(tags: Set<String> = options.selectedTags.value) {
        viewModelScope.launch {
            runCatching { generator.generateRandom(tags, saveToHistory = true) }
                .onSuccess {
                    _word.value = it.word
                    _definition.value = it.definition
                }
        }
    }
}
