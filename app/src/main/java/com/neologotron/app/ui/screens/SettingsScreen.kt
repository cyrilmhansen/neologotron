package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neologotron.app.R

@Composable
fun SettingsScreen(onOpenDebug: () -> Unit, onOpenAbout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.title_settings), style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(id = R.string.placeholder_settings))
        Button(onClick = onOpenDebug, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = stringResource(id = R.string.action_open_debug))
        }
        Button(onClick = onOpenAbout, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = stringResource(id = R.string.action_open_about))
        }
    }
}
