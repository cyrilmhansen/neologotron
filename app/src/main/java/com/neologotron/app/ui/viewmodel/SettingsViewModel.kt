package com.neologotron.app.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.repo.SettingsRepository
import com.neologotron.app.domain.generator.GeneratorRules
import com.neologotron.app.theme.ThemeStyle
import com.neologotron.app.ui.AnimatedBackgroundIntensity

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repo: SettingsRepository,
    ) : ViewModel() {
        val theme: StateFlow<ThemeStyle> = repo.theme.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeStyle.MINIMAL)
        val darkTheme: StateFlow<Boolean> = repo.darkTheme.stateIn(viewModelScope, SharingStarted.Eagerly, true)
        val definitionMode: StateFlow<GeneratorRules.DefinitionMode> =
            repo.definitionMode.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                GeneratorRules.DefinitionMode.TECHNICAL,
            )
        val coherenceFilters: StateFlow<Boolean> = repo.coherenceFilters.stateIn(viewModelScope, SharingStarted.Eagerly, true)
        val shakeToGenerate: StateFlow<Boolean> = repo.shakeToGenerate.stateIn(viewModelScope, SharingStarted.Eagerly, false)
        val hapticOnShake: StateFlow<Boolean> = repo.hapticOnShake.stateIn(viewModelScope, SharingStarted.Eagerly, true)
        val shakeHintShown: StateFlow<Boolean> = repo.shakeHintShown.stateIn(viewModelScope, SharingStarted.Eagerly, false)
        val weightingIntensity: StateFlow<Float> = repo.weightingIntensity.stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)
        val animatedBackgroundsEnabled: StateFlow<Boolean> =
            repo.animatedBackgroundsEnabled.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false,
            )
        val animatedBackgroundsIntensity: StateFlow<AnimatedBackgroundIntensity> =
            repo.animatedBackgroundsIntensity
                .map { s -> runCatching { AnimatedBackgroundIntensity.valueOf(s) }.getOrDefault(AnimatedBackgroundIntensity.MEDIUM) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, AnimatedBackgroundIntensity.MEDIUM)
        val animatedBackgroundsDebug: StateFlow<Float> =
            repo.animatedBackgroundsDebug.stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)
        val simpleMixerEnabled: StateFlow<Boolean> = repo.simpleMixerEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)

        fun setTheme(style: ThemeStyle) {
            viewModelScope.launch { repo.setTheme(style) }
        }

        fun setDarkTheme(enabled: Boolean) {
            viewModelScope.launch { repo.setDarkTheme(enabled) }
        }

        fun setDefinitionMode(mode: GeneratorRules.DefinitionMode) {
            viewModelScope.launch { repo.setDefinitionMode(mode) }
        }

        fun setCoherenceFilters(enabled: Boolean) {
            viewModelScope.launch { repo.setCoherenceFilters(enabled) }
        }

        fun setShakeToGenerate(enabled: Boolean) {
            viewModelScope.launch { repo.setShakeToGenerate(enabled) }
        }

        fun setHapticOnShake(enabled: Boolean) {
            viewModelScope.launch { repo.setHapticOnShake(enabled) }
        }

        fun markShakeHintShown() {
            viewModelScope.launch { repo.setShakeHintShown(true) }
        }

        fun setWeightingIntensity(value: Float) {
            viewModelScope.launch { repo.setWeightingIntensity(value) }
        }

        fun setAnimatedBackgroundsEnabled(enabled: Boolean) {
            viewModelScope.launch { repo.setAnimatedBackgroundsEnabled(enabled) }
        }

        fun setAnimatedBackgroundsIntensity(value: AnimatedBackgroundIntensity) {
            viewModelScope.launch { repo.setAnimatedBackgroundsIntensity(value.name) }
        }

        fun setAnimatedBackgroundsDebug(value: Float) {
            viewModelScope.launch { repo.setAnimatedBackgroundsDebug(value) }
        }

        fun setSimpleMixerEnabled(enabled: Boolean) {
            viewModelScope.launch { repo.setSimpleMixerEnabled(enabled) }
        }
    }
