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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neologotron.app.R

@Composable
fun ThematicScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(id = R.string.title_thematic), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = false, onClick = { /* TODO */ }, label = { Text("nature") })
            FilterChip(selected = false, onClick = { /* TODO */ }, label = { Text("temps") })
            FilterChip(selected = false, onClick = { /* TODO */ }, label = { Text("technologie") })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = false, onClick = { /* TODO */ }, label = { Text("société") })
            FilterChip(selected = false, onClick = { /* TODO */ }, label = { Text("abstrait") })
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { /* TODO apply */ }) { Text(text = stringResource(id = R.string.action_apply)) }
            Button(onClick = { /* TODO reset */ }) { Text(text = stringResource(id = R.string.action_reset)) }
        }
    }
}

