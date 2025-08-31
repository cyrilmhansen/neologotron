package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.R
import com.neologotron.app.domain.generator.GeneratorRules
import com.neologotron.app.theme.ThemeStyle
import com.neologotron.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(onOpenDebug: () -> Unit, onOpenAbout: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val theme by vm.theme.collectAsState()
    val dark by vm.darkTheme.collectAsState()
    val defMode by vm.definitionMode.collectAsState()
    val filters by vm.coherenceFilters.collectAsState()
    val shake by vm.shakeToGenerate.collectAsState()
    val weight by vm.weightingIntensity.collectAsState()
    val animated by vm.animatedBackgroundsEnabled.collectAsState()
    val bgIntensity by vm.animatedBackgroundsIntensity.collectAsState()
    val simpleMixer by vm.simpleMixerEnabled.collectAsState()

    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(id = R.string.title_settings), style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.label_theme))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = theme == ThemeStyle.MINIMAL, onClick = { vm.setTheme(ThemeStyle.MINIMAL) })
            Text(text = stringResource(id = R.string.theme_minimal))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = theme == ThemeStyle.RETRO80S, onClick = { vm.setTheme(ThemeStyle.RETRO80S) })
            Text(text = stringResource(id = R.string.theme_retro))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = theme == ThemeStyle.CYBERPUNK, onClick = { vm.setTheme(ThemeStyle.CYBERPUNK) })
            Text(text = stringResource(id = R.string.theme_cyberpunk))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Switch(checked = dark, onCheckedChange = { vm.setDarkTheme(it) })
            Text(text = stringResource(id = R.string.theme_dark))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.label_def_mode))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = defMode == GeneratorRules.DefinitionMode.TECHNICAL, onClick = { vm.setDefinitionMode(GeneratorRules.DefinitionMode.TECHNICAL) })
            Text(text = stringResource(id = R.string.mode_technical))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = defMode == GeneratorRules.DefinitionMode.POETIC, onClick = { vm.setDefinitionMode(GeneratorRules.DefinitionMode.POETIC) })
            Text(text = stringResource(id = R.string.mode_poetic))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.label_options))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = filters, onCheckedChange = { vm.setCoherenceFilters(it) })
            Text(text = stringResource(id = R.string.option_coherence_filters))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = shake, onCheckedChange = { vm.setShakeToGenerate(it) })
            Text(text = stringResource(id = R.string.option_shake_generate))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val haptic by vm.hapticOnShake.collectAsState()
            Switch(checked = haptic, onCheckedChange = { vm.setHapticOnShake(it) })
            Text(text = stringResource(id = R.string.option_haptic_on_shake))
        }

        // Animated backgrounds
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.label_animated_backgrounds))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = animated, onCheckedChange = { vm.setAnimatedBackgroundsEnabled(it) })
            Text(text = stringResource(id = R.string.option_animated_backgrounds))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(id = R.string.label_background_intensity), modifier = Modifier.padding(end = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(enabled = animated, selected = bgIntensity == com.neologotron.app.ui.AnimatedBackgroundIntensity.LOW, onClick = { vm.setAnimatedBackgroundsIntensity(com.neologotron.app.ui.AnimatedBackgroundIntensity.LOW) })
            Text(text = stringResource(id = R.string.intensity_low))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(enabled = animated, selected = bgIntensity == com.neologotron.app.ui.AnimatedBackgroundIntensity.MEDIUM, onClick = { vm.setAnimatedBackgroundsIntensity(com.neologotron.app.ui.AnimatedBackgroundIntensity.MEDIUM) })
            Text(text = stringResource(id = R.string.intensity_medium))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(enabled = animated, selected = bgIntensity == com.neologotron.app.ui.AnimatedBackgroundIntensity.HIGH, onClick = { vm.setAnimatedBackgroundsIntensity(com.neologotron.app.ui.AnimatedBackgroundIntensity.HIGH) })
            Text(text = stringResource(id = R.string.intensity_high))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.label_thematic_bias))
        Text(
            text = stringResource(id = R.string.bias_desc),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = weight < 0.75f, onClick = { vm.setWeightingIntensity(0.5f) })
            Text(text = stringResource(id = R.string.bias_low))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = weight >= 0.75f && weight <= 1.25f, onClick = { vm.setWeightingIntensity(1.0f) })
            Text(text = stringResource(id = R.string.bias_normal))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = weight > 1.25f, onClick = { vm.setWeightingIntensity(1.5f) })
            Text(text = stringResource(id = R.string.bias_high))
        }

        // Simple Word Grinder (Moulinette simple)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.label_simple_mixer))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = simpleMixer, onCheckedChange = { vm.setSimpleMixerEnabled(it) })
            Text(text = stringResource(id = R.string.option_simple_mixer))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenDebug, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = stringResource(id = R.string.action_open_debug))
        }
        Button(onClick = onOpenAbout, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = stringResource(id = R.string.action_open_about))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
