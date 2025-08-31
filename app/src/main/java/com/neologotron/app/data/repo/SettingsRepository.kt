package com.neologotron.app.data.repo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.neologotron.app.domain.generator.GeneratorRules
import com.neologotron.app.theme.ThemeStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "user_prefs")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_style")
    private val darkKey = booleanPreferencesKey("dark_theme")
    private val defModeKey = stringPreferencesKey("definition_mode")
    private val filtersKey = booleanPreferencesKey("coherence_filters")
    private val shakeKey = booleanPreferencesKey("shake_generate")
    private val hapticOnShakeKey = booleanPreferencesKey("haptic_on_shake")
    private val shakeHintShownKey = booleanPreferencesKey("shake_hint_shown")
    private val selectedTagsKey = stringPreferencesKey("selected_thematic_tags")
    private val weightingIntensityKey = floatPreferencesKey("weighting_intensity")
    private val animatedBackgroundsKey = booleanPreferencesKey("animated_backgrounds_enabled")
    private val animatedBgIntensityKey = stringPreferencesKey("animated_backgrounds_intensity")
    private val simpleMixerKey = booleanPreferencesKey("simple_mixer_enabled")

    val theme: Flow<ThemeStyle> = context.settingsStore.data.map { p ->
        when (p[themeKey]) {
            ThemeStyle.RETRO80S.name -> ThemeStyle.RETRO80S
            ThemeStyle.CYBERPUNK.name -> ThemeStyle.CYBERPUNK
            else -> ThemeStyle.MINIMAL
        }
    }

    val darkTheme: Flow<Boolean> = context.settingsStore.data.map { p -> p[darkKey] ?: true }

    val definitionMode: Flow<GeneratorRules.DefinitionMode> = context.settingsStore.data.map { p ->
        when (p[defModeKey]) {
            GeneratorRules.DefinitionMode.POETIC.name -> GeneratorRules.DefinitionMode.POETIC
            else -> GeneratorRules.DefinitionMode.TECHNICAL
        }
    }

    val coherenceFilters: Flow<Boolean> = context.settingsStore.data.map { p -> p[filtersKey] ?: true }
    val shakeToGenerate: Flow<Boolean> = context.settingsStore.data.map { p -> p[shakeKey] ?: false }
    val hapticOnShake: Flow<Boolean> = context.settingsStore.data.map { p -> p[hapticOnShakeKey] ?: true }
    val shakeHintShown: Flow<Boolean> = context.settingsStore.data.map { p -> p[shakeHintShownKey] ?: false }
    val selectedTags: Flow<Set<String>> = context.settingsStore.data.map { p ->
        p[selectedTagsKey]?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }
    val weightingIntensity: Flow<Float> = context.settingsStore.data.map { p -> p[weightingIntensityKey] ?: 1.0f }
    val animatedBackgroundsEnabled: Flow<Boolean> = context.settingsStore.data.map { p -> p[animatedBackgroundsKey] ?: false }
    val animatedBackgroundsIntensity: Flow<String> = context.settingsStore.data.map { p -> p[animatedBgIntensityKey] ?: "MEDIUM" }
    val simpleMixerEnabled: Flow<Boolean> = context.settingsStore.data.map { p -> p[simpleMixerKey] ?: false }

    suspend fun setTheme(style: ThemeStyle) { context.settingsStore.edit { it[themeKey] = style.name } }
    suspend fun setDarkTheme(enabled: Boolean) { context.settingsStore.edit { it[darkKey] = enabled } }
    suspend fun setDefinitionMode(mode: GeneratorRules.DefinitionMode) { context.settingsStore.edit { it[defModeKey] = mode.name } }
    suspend fun setCoherenceFilters(enabled: Boolean) { context.settingsStore.edit { it[filtersKey] = enabled } }
    suspend fun setShakeToGenerate(enabled: Boolean) { context.settingsStore.edit { it[shakeKey] = enabled } }
    suspend fun setHapticOnShake(enabled: Boolean) { context.settingsStore.edit { it[hapticOnShakeKey] = enabled } }
    suspend fun setShakeHintShown(shown: Boolean = true) { context.settingsStore.edit { it[shakeHintShownKey] = shown } }
    suspend fun setSelectedTags(tags: Set<String>) {
        val serialized = tags.sorted().joinToString(",")
        context.settingsStore.edit { it[selectedTagsKey] = serialized }
    }
    suspend fun setWeightingIntensity(value: Float) { context.settingsStore.edit { it[weightingIntensityKey] = value } }
    suspend fun setAnimatedBackgroundsEnabled(enabled: Boolean) { context.settingsStore.edit { it[animatedBackgroundsKey] = enabled } }
    suspend fun setAnimatedBackgroundsIntensity(value: String) { context.settingsStore.edit { it[animatedBgIntensityKey] = value } }
    suspend fun setSimpleMixerEnabled(enabled: Boolean) { context.settingsStore.edit { it[simpleMixerKey] = enabled } }
}
