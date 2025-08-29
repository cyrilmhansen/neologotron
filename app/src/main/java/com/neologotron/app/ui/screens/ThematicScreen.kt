package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.ThematicViewModel

@Composable
fun ThematicScreen(onOpenDetail: (String) -> Unit, vm: ThematicViewModel = hiltViewModel()) {
    val tags by vm.tags.collectAsState()
    val selected by vm.selected.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(id = R.string.title_thematic), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        val rowSpacing = 8.dp
        Row(horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
            tags.take(5).forEach { tag ->
                FilterChip(selected = selected.contains(tag), onClick = { vm.toggle(tag) }, label = { Text(tag) })
            }
        }
        if (tags.size > 5) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
                tags.drop(5).take(5).forEach { tag ->
                    FilterChip(selected = selected.contains(tag), onClick = { vm.toggle(tag) }, label = { Text(tag) })
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.apply() }) { Text(text = stringResource(id = R.string.action_apply)) }
            Button(onClick = { vm.reset() }) { Text(text = stringResource(id = R.string.action_reset)) }
            Button(onClick = { vm.generateAndOpen(onOpenDetail) }) { Text(text = stringResource(id = R.string.action_open_detail_preview)) }
        }
    }
}
