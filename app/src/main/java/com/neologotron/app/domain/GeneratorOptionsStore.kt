package com.neologotron.app.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratorOptionsStore
    @Inject
    constructor() {
        private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
        val selectedTags: StateFlow<Set<String>> = _selectedTags

        fun setTags(tags: Set<String>) {
            _selectedTags.value = tags
        }

        fun clear() {
            _selectedTags.value = emptySet()
        }
    }
