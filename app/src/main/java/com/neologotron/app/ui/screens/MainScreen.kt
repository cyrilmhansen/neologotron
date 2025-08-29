package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.neologotron.app.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onOpenThematic: () -> Unit,
    onOpenWorkshop: () -> Unit,
    vm: MainViewModel = hiltViewModel()
) {
    val word by vm.word.collectAsState()
    val definition by vm.definition.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(24.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = definition,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
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
