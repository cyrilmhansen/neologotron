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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(onOpenDetail: (String) -> Unit, vm: FavoritesViewModel = hiltViewModel()) {
    val items by vm.items.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh when the screen returns to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.title_favorites), style = MaterialTheme.typography.headlineSmall) },
                supportingContent = { if (items.isEmpty()) Text(text = stringResource(id = R.string.placeholder_favorites)) }
            )
            HorizontalDivider()
        }
        items(items) { fav ->
            ListItem(
                headlineContent = { Text(fav.word) },
                supportingContent = { Text(fav.definition) },
                modifier = Modifier.clickable { onOpenDetail(fav.word) }
            )
            HorizontalDivider()
        }
    }
}
