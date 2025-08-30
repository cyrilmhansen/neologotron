package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.data.repo.LexemeRepository
import com.neologotron.app.data.repo.HistoryRepository
import com.neologotron.app.domain.generator.GeneratorRules
import com.neologotron.app.domain.generator.GeneratorService
import com.neologotron.app.data.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkshopViewModel @Inject constructor(
    private val repo: LexemeRepository,
    private val generator: GeneratorService,
    private val history: HistoryRepository,
    private val settings: SettingsRepository
) : ViewModel() {
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
        viewModelScope.launch {
            _prefixes.value = repo.listPrefixesByTag("").take(10)
            _roots.value = repo.listRootsByTag("").take(10)
            _suffixes.value = repo.listSuffixesByTag("").take(10)
            settings.coherenceFilters.collect { enabled -> _filtersEnabled.value = enabled }
        }
    }

    fun searchPrefixes(query: String) {
        viewModelScope.launch {
            _prefixes.value = if (query.isBlank()) repo.listPrefixesByTag("") else repo.searchPrefixes(query)
        }
    }

    fun searchRoots(query: String) {
        viewModelScope.launch {
            _roots.value = if (query.isBlank()) repo.listRootsByTag("") else repo.searchRoots(query)
        }
    }

    fun searchSuffixes(query: String) {
        viewModelScope.launch {
            _suffixes.value = if (query.isBlank()) repo.listSuffixesByTag("") else repo.searchSuffixes(query)
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

    fun previewSelectedAndOpen(onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit) {
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

    fun commitAndOpen(onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit) {
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
            runCatching { history.add(word, def, decomp, mode = "manual") }
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
