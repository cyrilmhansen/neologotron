package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onOpenThematic: () -> Unit,
    onOpenWorkshop: () -> Unit,
    vm: MainViewModel = hiltViewModel()
) {
    val word by vm.word.collectAsState()
    val definition by vm.definition.collectAsState()
    val isFavorite by vm.isFavorite.collectAsState()
    val activeTags by vm.activeTags.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.favoriteToggled.collect { added ->
            // Avoid stacking/duplicating snackbars; ignore if one is visible
            if (snackbarHostState.currentSnackbarData == null) {
                val msg = if (added) R.string.msg_favorite_added else R.string.msg_favorite_removed
                snackbarHostState.showSnackbar(
                    message = context.getString(msg),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(PaddingValues(24.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { vm.toggleFavorite() }, modifier = Modifier.padding(start = 12.dp)) {
                    if (isFavorite) {
                        Icon(Icons.Filled.Favorite, contentDescription = stringResource(id = R.string.action_unfavorite))
                    } else {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = stringResource(id = R.string.action_favorite))
                    }
                }
            }
            Text(
                text = definition,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
            if (activeTags.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_active_tags, activeTags.joinToString()),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
                Button(onClick = { vm.clearTags() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text(text = stringResource(id = R.string.action_clear_tags))
                }
            }
            Button(onClick = { vm.generate() }, modifier = Modifier.padding(top = 24.dp)) {
                Text(text = stringResource(id = R.string.action_generate))
            }
            Button(onClick = onOpenThematic, modifier = Modifier.padding(top = 12.dp)) {
                Text(text = stringResource(id = R.string.action_open_thematic))
            }
            Button(onClick = onOpenWorkshop, modifier = Modifier.padding(top = 12.dp)) {
                Text(text = stringResource(id = R.string.action_open_workshop))
            }
        }
    }
}
