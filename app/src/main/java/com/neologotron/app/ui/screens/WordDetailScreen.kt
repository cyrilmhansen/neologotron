package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.R
import com.neologotron.app.ui.copyToClipboard
import com.neologotron.app.ui.shareWord
import com.neologotron.app.ui.viewmodel.WordDetailViewModel
import com.neologotron.app.ui.viewmodel.SettingsViewModel
import com.neologotron.app.domain.generator.GeneratorRules
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(onBack: () -> Unit, vm: WordDetailViewModel = hiltViewModel(), settingsVm: SettingsViewModel = hiltViewModel()) {
    val word by vm.word.collectAsState()
    val definition by vm.definition.collectAsState()
    val decomposition by vm.decomposition.collectAsState()
    val isFavorite by vm.isFavorite.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val defMode by settingsVm.definitionMode.collectAsState(initial = GeneratorRules.DefinitionMode.TECHNICAL)

    LaunchedEffect(Unit) {
        vm.favoriteToggled.collect { added ->
            if (snackbarHostState.currentSnackbarData == null) {
                val msg = if (added) R.string.msg_favorite_added else R.string.msg_favorite_removed
                snackbarHostState.showSnackbar(
                    message = context.getString(msg),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.title_detail)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = word, style = MaterialTheme.typography.headlineLarge)
                IconButton(onClick = {
                    copyToClipboard(context, context.getString(R.string.action_copy_word), word)
                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.msg_copied)) }
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(id = R.string.action_copy_word))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.label_definition), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    copyToClipboard(context, context.getString(R.string.action_copy_definition), definition)
                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.msg_copied)) }
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(id = R.string.action_copy_definition))
                }
            }
            Text(
                text = if (definition.isNotBlank()) definition else stringResource(id = R.string.placeholder_definition),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.label_etymology), style = MaterialTheme.typography.titleMedium)
            Text(text = if (decomposition.isNotBlank()) decomposition else stringResource(id = R.string.placeholder_decomposition))

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.label_def_mode), modifier = Modifier.padding(end = 8.dp))
                androidx.compose.material3.FilterChip(
                    selected = defMode == GeneratorRules.DefinitionMode.TECHNICAL,
                    onClick = { settingsVm.setDefinitionMode(GeneratorRules.DefinitionMode.TECHNICAL) },
                    label = { Text(stringResource(id = R.string.mode_technical)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.FilterChip(
                    selected = defMode == GeneratorRules.DefinitionMode.POETIC,
                    onClick = { settingsVm.setDefinitionMode(GeneratorRules.DefinitionMode.POETIC) },
                    label = { Text(stringResource(id = R.string.mode_poetic)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { shareWord(context, word, definition) }) { Text(text = stringResource(id = R.string.action_share)) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { vm.toggleFavorite() }) { Text(text = stringResource(id = if (isFavorite) R.string.action_unfavorite else R.string.action_favorite)) }
        }
    }
}
