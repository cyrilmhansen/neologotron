package com.neologotron.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.HistoryViewModel
import java.text.DateFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(onOpenDetail: (String) -> Unit, vm: HistoryViewModel = hiltViewModel()) {
    val items by vm.items.collectAsState()
    val df = DateFormat.getDateTimeInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { _ ->
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.title_history), style = MaterialTheme.typography.headlineSmall) },
                    supportingContent = { if (items.isEmpty()) Text(text = stringResource(id = R.string.placeholder_history)) }
                )
                HorizontalDivider()
            }
            items(items, key = { it.id }) { h ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { state ->
                        if (state == DismissValue.DismissedToEnd || state == DismissValue.DismissedToStart) {
                            val deleted = h
                            vm.remove(deleted.id)
                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.msg_history_deleted),
                                    actionLabel = context.getString(R.string.action_undo),
                                    duration = SnackbarDuration.Short
                                )
                                if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    vm.undoInsert(deleted)
                                }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )
                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                    background = {
                        val isDefault = dismissState.targetValue == DismissValue.Default
                        val isStart = dismissState.targetValue == DismissValue.DismissedToStart
                        val isEnd = dismissState.targetValue == DismissValue.DismissedToEnd

                        // Different target colors by direction; animate between them
                        val targetBg = when {
                            isStart -> MaterialTheme.colorScheme.errorContainer
                            isEnd -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val bg by animateColorAsState(targetBg, label = "historySwipeBg")

                        val targetTint = when {
                            isStart -> MaterialTheme.colorScheme.onErrorContainer
                            isEnd -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val tint by animateColorAsState(targetTint, label = "historySwipeTint")

                        val iconScale by animateFloatAsState(
                            targetValue = if (isDefault) 0.85f else 1.15f,
                            label = "historyIconScale"
                        )
                        val alignment = when (dismissState.dismissDirection) {
                            DismissDirection.StartToEnd -> Alignment.CenterStart
                            DismissDirection.EndToStart -> Alignment.CenterEnd
                            null -> Alignment.Center
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bg),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = R.string.action_delete),
                                tint = tint,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .scale(iconScale)
                            )
                        }
                    },
                    dismissContent = {
                        ListItem(
                            headlineContent = { Text(h.word) },
                            supportingContent = { Text(h.definition) },
                            overlineContent = { Text(df.format(java.util.Date(h.timestamp))) },
                            modifier = Modifier.clickable { onOpenDetail(h.word) }
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
