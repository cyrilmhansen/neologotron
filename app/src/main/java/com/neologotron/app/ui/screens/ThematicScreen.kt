package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThematicScreen(onOpenDetail: (String, String?, String?, String?, String?, String?, String?, String?, String?, String?, String?) -> Unit, vm: ThematicViewModel = hiltViewModel()) {
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                FilterChip(selected = selected.contains(tag), onClick = { vm.toggle(tag) }, label = { Text(tag) })
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.reset() }) { Text(text = stringResource(id = R.string.action_reset)) }
            Button(onClick = { vm.generateAndOpen(onOpenDetail) }) { Text(text = stringResource(id = R.string.action_open_detail_preview)) }
        }
    }
}
