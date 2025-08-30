package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.ThematicViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.neologotron.app.ui.UiState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.remember

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThematicScreen(onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit, vm: ThematicViewModel = hiltViewModel()) {
    val tagsState by vm.tags.collectAsState()
    val selected by vm.selected.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    if (tagsState is UiState.Error) {
        LaunchedEffect(tagsState) {
            val res = snackbarHost.showSnackbar(
                message = (tagsState as UiState.Error).message ?: ctx.getString(R.string.error_generic),
                actionLabel = ctx.getString(R.string.action_retry),
                duration = SnackbarDuration.Short
            )
            if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) vm.refreshTags()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = stringResource(id = R.string.title_thematic), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            val isLoading = tagsState is UiState.Loading
            val tags = (tagsState as? UiState.Data<List<String>>)?.value.orEmpty()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isLoading) {
                    repeat(12) {
                        androidx.compose.foundation.layout.Box(
                            Modifier.width(80.dp).height(28.dp).background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                } else {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selected.contains(tag),
                            onClick = { vm.toggle(tag) },
                            label = { Text(tag) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { vm.reset() }) { Text(text = stringResource(id = R.string.action_reset)) }
                Button(onClick = { vm.generateAndOpen(onOpenDetail) }) { Text(text = stringResource(id = R.string.action_open_detail_preview)) }
            }
        }
    }
}
