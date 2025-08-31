package com.neologotron.app.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.data.repo.HistoryRepository
import com.neologotron.app.data.repo.LexemeRepository
import com.neologotron.app.data.repo.SettingsRepository
import com.neologotron.app.domain.generator.GeneratorRules
import com.neologotron.app.domain.generator.GeneratorService
import com.neologotron.app.ui.UiState

@HiltViewModel
class WorkshopViewModel
    @Inject
    constructor(
        private val repo: LexemeRepository,
        private val generator: GeneratorService,
        private val history: HistoryRepository,
        private val settings: SettingsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
        val uiState: StateFlow<UiState<Unit>> = _uiState
        private val _prefixes = MutableStateFlow<List<PrefixEntity>>(emptyList())
        val prefixes: StateFlow<List<PrefixEntity>> = _prefixes

        private val _roots = MutableStateFlow<List<RootEntity>>(emptyList())
        val roots: StateFlow<List<RootEntity>> = _roots

        private val _suffixes = MutableStateFlow<List<SuffixEntity>>(emptyList())
        val suffixes: StateFlow<List<SuffixEntity>> = _suffixes

        private val _selectedPrefix = MutableStateFlow<PrefixEntity?>(null)
        val selectedPrefix: StateFlow<PrefixEntity?> = _selectedPrefix.asStateFlow()

        private val _selectedRoot = MutableStateFlow<RootEntity?>(null)
        val selectedRoot: StateFlow<RootEntity?> = _selectedRoot.asStateFlow()

        private val _selectedSuffix = MutableStateFlow<SuffixEntity?>(null)
        val selectedSuffix: StateFlow<SuffixEntity?> = _selectedSuffix.asStateFlow()

        private val _previewWord = MutableStateFlow("")
        val previewWord: StateFlow<String> = _previewWord.asStateFlow()

        private val _previewDefinition = MutableStateFlow("")
        val previewDefinition: StateFlow<String> = _previewDefinition.asStateFlow()

        private val _previewDecomposition = MutableStateFlow("")
        val previewDecomposition: StateFlow<String> = _previewDecomposition.asStateFlow()

        private val _filtersEnabled = MutableStateFlow(true)
        val filtersEnabled: StateFlow<Boolean> = _filtersEnabled.asStateFlow()

        init {
            refreshInitial()
        }

        fun refreshInitial() {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                runCatching {
                    val p = repo.listPrefixesByTag("").take(10)
                    val r = repo.listRootsByTag("").take(10)
                    val s = repo.listSuffixesByTag("").take(10)
                    Triple(p, r, s)
                }.onSuccess { (p, r, s) ->
                    _prefixes.value = p
                    _roots.value = r
                    _suffixes.value = s
                    _uiState.value = UiState.Data(Unit)
                }.onFailure { e ->
                    _uiState.value = UiState.Error(e.message)
                }
                // Keep filters synced regardless
                viewModelScope.launch { settings.coherenceFilters.collect { enabled -> _filtersEnabled.value = enabled } }
            }
        }

        fun searchPrefixes(query: String) {
            viewModelScope.launch {
                runCatching { if (query.isBlank()) repo.listPrefixesByTag("") else repo.searchPrefixes(query) }
                    .onSuccess { _prefixes.value = it }
                    .onFailure { _uiState.value = UiState.Error(it.message) }
            }
        }

        fun searchRoots(query: String) {
            viewModelScope.launch {
                runCatching { if (query.isBlank()) repo.listRootsByTag("") else repo.searchRoots(query) }
                    .onSuccess { _roots.value = it }
                    .onFailure { _uiState.value = UiState.Error(it.message) }
            }
        }

        fun searchSuffixes(query: String) {
            viewModelScope.launch {
                runCatching { if (query.isBlank()) repo.listSuffixesByTag("") else repo.searchSuffixes(query) }
                    .onSuccess { _suffixes.value = it }
                    .onFailure { _uiState.value = UiState.Error(it.message) }
            }
        }

        fun selectPrefix(item: PrefixEntity) {
            _selectedPrefix.value = item
            recomputePreview()
        }

        fun selectRoot(item: RootEntity) {
            _selectedRoot.value = item
            recomputePreview()
        }

        fun selectSuffix(item: SuffixEntity) {
            _selectedSuffix.value = item
            recomputePreview()
        }

        private fun recomputePreview() {
            val p = _selectedPrefix.value
            val r = _selectedRoot.value
            val s = _selectedSuffix.value
            if (p != null && r != null && s != null) {
                val word = GeneratorRules.composeWord(p.form, r.form, s.form, r.connectorPref, useFilters = _filtersEnabled.value).word
                val def = GeneratorRules.composeDefinition(r.gloss, s.posOut, s.defTemplate, s.tags)
                _previewWord.value = word
                _previewDefinition.value = def
                _previewDecomposition.value = "${p.form} + ${r.form} + ${s.form}"
            } else {
                _previewWord.value = ""
                _previewDefinition.value = ""
                _previewDecomposition.value = ""
            }
        }

        fun previewSelectedAndOpen(
            onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit,
        ) {
            val p = _selectedPrefix.value
            val r = _selectedRoot.value
            val s = _selectedSuffix.value
            if (p == null || r == null || s == null) return
            val word = GeneratorRules.composeWord(p.form, r.form, s.form, r.connectorPref, useFilters = _filtersEnabled.value).word
            val def = GeneratorRules.composeDefinition(r.gloss, s.posOut, s.defTemplate, s.tags)
            val decomp = "${p.form} + ${r.form} + ${s.form}"
            _previewWord.value = word
            _previewDefinition.value = def
            _previewDecomposition.value = decomp
            onOpenDetail(
                word,
                def,
                decomp,
                p.form,
                r.form,
                s.form,
                r.gloss,
                r.connectorPref,
                s.posOut,
                s.defTemplate,
                s.tags,
            )
        }

        fun commitAndOpen(
            onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit,
        ) {
            val p = _selectedPrefix.value
            val r = _selectedRoot.value
            val s = _selectedSuffix.value
            if (p == null || r == null || s == null) return
            val word = GeneratorRules.composeWord(p.form, r.form, s.form, r.connectorPref, useFilters = _filtersEnabled.value).word
            val def = GeneratorRules.composeDefinition(r.gloss, s.posOut, s.defTemplate, s.tags)
            val decomp = "${p.form} + ${r.form} + ${s.form}"
            _previewWord.value = word
            _previewDefinition.value = def
            _previewDecomposition.value = decomp
            viewModelScope.launch {
                runCatching {
                    history.add(
                        word = word,
                        definition = def,
                        decomposition = decomp,
                        mode = "manual",
                        prefixForm = p.form,
                        rootForm = r.form,
                        suffixForm = s.form,
                        rootGloss = r.gloss,
                        rootConnectorPref = r.connectorPref,
                        suffixPosOut = s.posOut,
                        suffixDefTemplate = s.defTemplate,
                        suffixTags = s.tags,
                    )
                }
                    .onSuccess {
                        onOpenDetail(
                            word,
                            null,
                            null,
                            p.form,
                            r.form,
                            s.form,
                            r.gloss,
                            r.connectorPref,
                            s.posOut,
                            s.defTemplate,
                            s.tags,
                        )
                    }
            }
        }
    }
