package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.WorkshopViewModel

@Composable
fun WorkshopScreen(onOpenDetail: (String) -> Unit, vm: WorkshopViewModel = hiltViewModel()) {
    val prefixes by vm.prefixes.collectAsState()
    val roots by vm.roots.collectAsState()
    val suffixes by vm.suffixes.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(id = R.string.title_workshop), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { /* TODO prefix list */ }) { Text(text = stringResource(id = R.string.action_select_prefix)) }
            Button(onClick = { /* TODO root list */ }) { Text(text = stringResource(id = R.string.action_select_root)) }
            Button(onClick = { /* TODO suffix list */ }) { Text(text = stringResource(id = R.string.action_select_suffix)) }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(id = R.string.label_preview), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.placeholder_word),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Start
        )
        Text(
            text = stringResource(id = R.string.placeholder_definition),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { /* TODO commit */ }) { Text(text = stringResource(id = R.string.action_compose_word)) }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { vm.generateAndOpen(onOpenDetail) }) { Text(text = stringResource(id = R.string.action_open_detail_preview)) }

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
