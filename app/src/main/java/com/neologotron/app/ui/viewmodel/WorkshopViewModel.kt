package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.data.repo.LexemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkshopViewModel @Inject constructor(
    private val repo: LexemeRepository
) : ViewModel() {
    private val _prefixes = MutableStateFlow<List<PrefixEntity>>(emptyList())
    val prefixes: StateFlow<List<PrefixEntity>> = _prefixes

    private val _roots = MutableStateFlow<List<RootEntity>>(emptyList())
    val roots: StateFlow<List<RootEntity>> = _roots

    private val _suffixes = MutableStateFlow<List<SuffixEntity>>(emptyList())
    val suffixes: StateFlow<List<SuffixEntity>> = _suffixes

    init {
        viewModelScope.launch {
            _prefixes.value = repo.listPrefixesByTag("").take(10)
            _roots.value = repo.listRootsByTag("").take(10)
            _suffixes.value = repo.listSuffixesByTag("").take(10)
        }
    }
}

