package com.neologotron.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.neologotron.app.R

@Composable
fun FavoritesScreen(onOpenDetail: (String) -> Unit) {
    val samples = listOf("Noctiluxe", "Photomorphe")
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.title_favorites), style = MaterialTheme.typography.headlineSmall) },
                supportingContent = { Text(text = stringResource(id = R.string.placeholder_favorites)) }
            )
            HorizontalDivider()
        }
        items(samples) { word ->
            ListItem(
                headlineContent = { Text(word) },
                modifier = Modifier.clickable { onOpenDetail(word) }
            )
            HorizontalDivider()
        }
    }
}
