package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.copyToClipboard
import com.neologotron.app.ui.shareWord
import com.neologotron.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onOpenThematic: () -> Unit,
    onOpenWorkshop: () -> Unit,
    vm: MainViewModel = hiltViewModel()
) {
    val word by vm.word.collectAsState()
    val definition by vm.definition.collectAsState()
    val isFavorite by vm.isFavorite.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                IconButton(onClick = { shareWord(context, word, definition) }) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(id = R.string.action_share))
                }
                IconButton(onClick = {
                    copyToClipboard(context, context.getString(R.string.action_copy_word), word)
                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.msg_copied)) }
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(id = R.string.action_copy_word))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = definition,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = {
                    copyToClipboard(context, context.getString(R.string.action_copy_definition), definition)
                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.msg_copied)) }
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(id = R.string.action_copy_definition))
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
