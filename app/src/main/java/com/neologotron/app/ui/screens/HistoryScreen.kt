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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.HistoryViewModel
import java.text.DateFormat

@Composable
fun HistoryScreen(onOpenDetail: (String) -> Unit, vm: HistoryViewModel = hiltViewModel()) {
    val items by vm.items.collectAsState()
    val df = DateFormat.getDateTimeInstance()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.title_history), style = MaterialTheme.typography.headlineSmall) },
                supportingContent = { if (items.isEmpty()) Text(text = stringResource(id = R.string.placeholder_history)) }
            )
            HorizontalDivider()
        }
        items(items) { h ->
            ListItem(
                headlineContent = { Text(h.word) },
                supportingContent = { Text(h.definition) },
                overlineContent = { Text(df.format(java.util.Date(h.timestamp))) },
                modifier = Modifier.clickable { onOpenDetail(h.word) }
            )
            HorizontalDivider()
        }
    }
}
