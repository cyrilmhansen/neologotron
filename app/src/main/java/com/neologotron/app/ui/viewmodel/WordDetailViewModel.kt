package com.neologotron.app.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.repo.FavoriteRepository
import com.neologotron.app.data.repo.HistoryRepository
import com.neologotron.app.data.repo.SettingsRepository
import com.neologotron.app.domain.generator.GeneratorRules

@HiltViewModel
class WordDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val history: HistoryRepository,
        private val favorites: FavoriteRepository,
        private val settings: SettingsRepository,
    ) : ViewModel() {
        private data class MorphMeta(
            val pform: String?,
            val rform: String,
            val sform: String,
            val rgloss: String?,
            val rconn: String?,
            val spos: String?,
            val sdef: String?,
            val stags: String?,
        )

        private val wordArg: String = savedStateHandle.get<String>("word").orEmpty()
        private val defArg: String = savedStateHandle.get<String>("def").orEmpty()
        private val decompArg: String = savedStateHandle.get<String>("decomp").orEmpty()
        private val pformArg: String = savedStateHandle.get<String>("pform").orEmpty()
        private val rformArg: String = savedStateHandle.get<String>("rform").orEmpty()
        private val sformArg: String = savedStateHandle.get<String>("sform").orEmpty()
        private val rglossArg: String = savedStateHandle.get<String>("rgloss").orEmpty()
        private val rconnArg: String = savedStateHandle.get<String>("rconn").orEmpty()
        private val sposArg: String = savedStateHandle.get<String>("spos").orEmpty()
        private val sdefArg: String = savedStateHandle.get<String>("sdef").orEmpty()
        private val stagsArg: String = savedStateHandle.get<String>("stags").orEmpty()
        private val srcArg: String = savedStateHandle.get<String>("src").orEmpty()

        private val _word = MutableStateFlow(wordArg)
        val word: StateFlow<String> = _word

        private val _definition = MutableStateFlow("")
        val definition: StateFlow<String> = _definition

        private val _decomposition = MutableStateFlow("")
        val decomposition: StateFlow<String> = _decomposition

        private val _mode = MutableStateFlow("random")
        val mode: StateFlow<String> = _mode

        private val _isFavorite = MutableStateFlow(false)
        val isFavorite: StateFlow<Boolean> = _isFavorite

        private val _favoriteToggled = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
        val favoriteToggled: SharedFlow<Boolean> = _favoriteToggled

        private val _sources = MutableStateFlow(srcArg.ifBlank { null })
        val sources: StateFlow<String?> = _sources

        private val _morphMeta =
            MutableStateFlow<MorphMeta?>(
                if (rformArg.isNotBlank() && sformArg.isNotBlank()) {
                    MorphMeta(
                        pform = pformArg.ifBlank { null },
                        rform = rformArg,
                        sform = sformArg,
                        rgloss = rglossArg.ifBlank { null },
                        rconn = rconnArg.ifBlank { null },
                        spos = sposArg.ifBlank { null },
                        sdef = sdefArg.ifBlank { null },
                        stags = stagsArg.ifBlank { null },
                    )
                } else {
                    null
                },
            )

        init {
            refresh()
            // Recompute on-the-fly when settings change and morph metadata is available
            viewModelScope.launch {
                combine(settings.definitionMode, settings.coherenceFilters, _morphMeta) { mode, filters, meta ->
                    Triple(mode, filters, meta)
                }.collect { (mode, filters, meta) ->
                    if (meta != null) {
                        val wordBuild =
                            GeneratorRules.composeWord(
                                prefixForm = meta.pform ?: "",
                                rootForm = meta.rform,
                                suffixForm = meta.sform,
                                connectorPref = meta.rconn,
                                useFilters = filters,
                            )
                        val newWord = wordBuild.word
                        val newDef =
                            GeneratorRules.composeDefinition(
                                rootGloss = meta.rgloss ?: meta.rform,
                                suffixPosOut = meta.spos,
                                defTemplate = meta.sdef,
                                tags = meta.stags,
                                mode = mode,
                            )
                        _word.value = newWord
                        _definition.value = newDef
                        _decomposition.value = listOfNotNull(meta.pform, meta.rform, meta.sform).joinToString(" + ")
                        _mode.value =
                            when (mode) {
                                GeneratorRules.DefinitionMode.TECHNICAL -> "technical"
                                GeneratorRules.DefinitionMode.POETIC -> "poetic"
                            }
                    }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                val latest = history.latestByWord(wordArg)
                if (latest != null) {
                    _sources.value = latest.sources
                    // If stored metadata exists, seed reactive recompute; else use stored strings
                    if (!latest.rootForm.isNullOrBlank() && !latest.suffixForm.isNullOrBlank()) {
                        _morphMeta.value =
                            MorphMeta(
                                pform = latest.prefixForm,
                                rform = latest.rootForm!!,
                                sform = latest.suffixForm!!,
                                rgloss = latest.rootGloss,
                                rconn = latest.rootConnectorPref,
                                spos = latest.suffixPosOut,
                                sdef = latest.suffixDefTemplate,
                                stags = latest.suffixTags,
                            )
                    } else {
                        _definition.value = latest.definition
                        _decomposition.value = latest.decomposition
                        _mode.value = latest.mode
                    }
                } else {
                    val fav = favorites.get(wordArg)
                    if (fav != null) {
                        if (!fav.rootForm.isNullOrBlank() && !fav.suffixForm.isNullOrBlank()) {
                            _morphMeta.value =
                                MorphMeta(
                                    pform = fav.prefixForm,
                                    rform = fav.rootForm!!,
                                    sform = fav.suffixForm!!,
                                    rgloss = fav.rootGloss,
                                    rconn = fav.rootConnectorPref,
                                    spos = fav.suffixPosOut,
                                    sdef = fav.suffixDefTemplate,
                                    stags = fav.suffixTags,
                                )
                        } else {
                            _definition.value = fav.definition
                            _decomposition.value = fav.decomposition
                            _mode.value = fav.mode
                        }
                    } else {
                        // Fallback to navigation-provided preview, if any
                        if (defArg.isNotBlank()) _definition.value = defArg
                        if (decompArg.isNotBlank()) _decomposition.value = decompArg
                        if (defArg.isNotBlank() || decompArg.isNotBlank()) _mode.value = "preview"
                    }
                }
                _isFavorite.value = favorites.isFavorited(wordArg)
            }
        }

        fun toggleFavorite() {
            viewModelScope.launch {
                val currentlyFav = favorites.isFavorited(wordArg)
                if (currentlyFav) {
                    favorites.remove(wordArg)
                } else {
                    favorites.add(
                        word = wordArg,
                        definition = _definition.value.ifBlank { "" },
                        decomposition = _decomposition.value.ifBlank { "" },
                        mode = _mode.value,
                        prefixForm = pformArg.ifBlank { null },
                        rootForm = rformArg.ifBlank { null },
                        suffixForm = sformArg.ifBlank { null },
                        rootGloss = rglossArg.ifBlank { null },
                        rootConnectorPref = rconnArg.ifBlank { null },
                        suffixPosOut = sposArg.ifBlank { null },
                        suffixDefTemplate = sdefArg.ifBlank { null },
                        suffixTags = stagsArg.ifBlank { null },
                    )
                }
                _isFavorite.value = !currentlyFav
                _favoriteToggled.emit(!currentlyFav)
            }
        }
    }
