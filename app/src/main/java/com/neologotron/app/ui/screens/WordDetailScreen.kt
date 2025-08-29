package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neologotron.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(word: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.title_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = word, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(id = R.string.label_definition), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(id = R.string.placeholder_definition), textAlign = TextAlign.Start)

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.label_etymology), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(id = R.string.placeholder_decomposition))

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { /* TODO: share */ }) { Text(text = stringResource(id = R.string.action_share)) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO: favorite */ }) { Text(text = stringResource(id = R.string.action_favorite)) }
        }
    }
}
