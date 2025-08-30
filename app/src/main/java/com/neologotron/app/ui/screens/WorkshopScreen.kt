package com.neologotron.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.data.entity.PrefixEntity
import com.neologotron.app.data.entity.RootEntity
import com.neologotron.app.data.entity.SuffixEntity
import com.neologotron.app.ui.viewmodel.WorkshopViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkshopScreen(
    onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit,
    vm: WorkshopViewModel = hiltViewModel()
) {
    val prefixes by vm.prefixes.collectAsState()
    val roots by vm.roots.collectAsState()
    val suffixes by vm.suffixes.collectAsState()
    val selectedPrefix by vm.selectedPrefix.collectAsState()
    val selectedRoot by vm.selectedRoot.collectAsState()
    val selectedSuffix by vm.selectedSuffix.collectAsState()
    val previewWord by vm.previewWord.collectAsState()
    val previewDefinition by vm.previewDefinition.collectAsState()
    val previewDecomposition by vm.previewDecomposition.collectAsState()

    var showPrefixPicker by remember { mutableStateOf(false) }
    var showRootPicker by remember { mutableStateOf(false) }
    var showSuffixPicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val context = LocalContext.current
    androidx.compose.material3.Scaffold(snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(id = R.string.title_workshop), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { showPrefixPicker = true }) { Text(text = stringResource(id = R.string.action_select_prefix)) }
            Button(onClick = { showRootPicker = true }) { Text(text = stringResource(id = R.string.action_select_root)) }
            Button(onClick = { showSuffixPicker = true }) { Text(text = stringResource(id = R.string.action_select_suffix)) }
        }

        if (showPrefixPicker) {
            SelectionDialog(
                title = stringResource(id = R.string.action_select_prefix),
                items = prefixes,
                labelOf = { it.form },
                secondaryOf = { it.gloss },
                onSearch = { q -> vm.searchPrefixes(q) },
                onSelect = { vm.selectPrefix(it); showPrefixPicker = false },
                onDismiss = { showPrefixPicker = false }
            )
        }
        if (showRootPicker) {
            SelectionDialog(
                title = stringResource(id = R.string.action_select_root),
                items = roots,
                labelOf = { it.form },
                secondaryOf = { it.gloss },
                onSearch = { q -> vm.searchRoots(q) },
                onSelect = { vm.selectRoot(it); showRootPicker = false },
                onDismiss = { showRootPicker = false }
            )
        }
        if (showSuffixPicker) {
            SelectionDialog(
                title = stringResource(id = R.string.action_select_suffix),
                items = suffixes,
                labelOf = { it.form },
                secondaryOf = { it.gloss },
                onSearch = { q -> vm.searchSuffixes(q) },
                onSelect = { vm.selectSuffix(it); showSuffixPicker = false },
                onDismiss = { showSuffixPicker = false }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(id = R.string.label_preview), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (selectedPrefix != null || selectedRoot != null || selectedSuffix != null) {
            Text(text = "Sélection: " + listOfNotNull(selectedPrefix?.form, selectedRoot?.form, selectedSuffix?.form).joinToString(" + "))
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = if (previewWord.isNotBlank()) previewWord else stringResource(id = R.string.placeholder_word),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Start
        )
        Text(
            text = if (previewDefinition.isNotBlank()) previewDefinition else stringResource(id = R.string.placeholder_definition),
            style = MaterialTheme.typography.bodyLarge
        )
        if (previewDecomposition.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = previewDecomposition, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        val canCompose = selectedPrefix != null && selectedRoot != null && selectedSuffix != null
        Button(onClick = { if (canCompose) vm.commitAndOpen(onOpenDetail) }, enabled = canCompose) { Text(text = stringResource(id = R.string.action_compose_word)) }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (canCompose) {
                vm.previewSelectedAndOpen(onOpenDetail)
            } else {
                // Guide the user to complete selection
                snackbarScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.msg_select_all_parts),
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                }
            }
        }) { Text(text = stringResource(id = R.string.action_open_detail_preview)) }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Préfixes (extraits)", style = MaterialTheme.typography.titleMedium)
        prefixes.take(5).forEach { item -> Text(text = "• ${item.form} (${item.gloss})") }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Racines (extraits)", style = MaterialTheme.typography.titleMedium)
        roots.take(5).forEach { item -> Text(text = "• ${item.form} (${item.gloss})") }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Suffixes (extraits)", style = MaterialTheme.typography.titleMedium)
        suffixes.take(5).forEach { item -> Text(text = "• ${item.form} (${item.gloss})") }
    }
    }
}

@Composable
private fun <T> SelectionDialog(
    title: String,
    items: List<T>,
    labelOf: (T) -> String,
    secondaryOf: (T) -> String?,
    onSearch: (String) -> Unit,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    LaunchedEffect(query) { onSearch(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(id = R.string.hint_search)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    items(items) { itx ->
                        ListItem(
                            headlineContent = { Text(text = labelOf(itx)) },
                            supportingContent = { secondaryOf(itx)?.let { s -> Text(text = s) } },
                            modifier = Modifier.clickable { onSelect(itx) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    )
}
