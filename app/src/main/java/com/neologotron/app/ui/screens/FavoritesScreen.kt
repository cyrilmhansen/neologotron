package com.neologotron.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.neologotron.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.FavoritesViewModel
import com.neologotron.app.ui.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoritesScreen(onOpenDetail: (String) -> Unit, vm: FavoritesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Refresh when the screen returns to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { _ ->
        if (state is UiState.Error) {
            LaunchedEffect(state) {
                val res = snackbarHostState.showSnackbar(
                    message = (state as UiState.Error).message ?: context.getString(R.string.error_generic),
                    actionLabel = context.getString(R.string.action_retry),
                    duration = SnackbarDuration.Short
                )
                if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) vm.refresh()
            }
        }
        val items = (state as? UiState.Data)?.value.orEmpty()
        val isLoading = state is UiState.Loading
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.title_favorites), style = MaterialTheme.typography.headlineSmall) },
                    supportingContent = {
                        if (!isLoading && items.isEmpty()) Text(text = stringResource(id = R.string.placeholder_favorites))
                        if (isLoading) Text(text = stringResource(id = R.string.label_loading))
                    }
                )
                HorizontalDivider()
            }
            if (isLoading) {
                items(6) {
                    ListItem(
                        headlineContent = { Box(Modifier.width(180.dp).height(16.dp).background(MaterialTheme.colorScheme.surfaceVariant)) },
                        supportingContent = { Box(Modifier.width(260.dp).height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant)) }
                    )
                    HorizontalDivider()
                }
            }
            items(items, key = { it.id }) { fav ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { state ->
                        if (state == DismissValue.DismissedToEnd || state == DismissValue.DismissedToStart) {
                            val deleted = fav
                            vm.remove(deleted.id)
                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.msg_favorite_deleted),
                                    actionLabel = context.getString(R.string.action_undo),
                                    duration = SnackbarDuration.Short
                                )
                                if (res == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    vm.undoInsert(deleted)
                                }
                            }
                            true
                        } else false
                    }
                )
                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                    background = {
                        val isDefault = dismissState.targetValue == DismissValue.Default
                        val isStart = dismissState.targetValue == DismissValue.DismissedToStart
                        val isEnd = dismissState.targetValue == DismissValue.DismissedToEnd

                        val targetBg = when {
                            isStart -> MaterialTheme.colorScheme.errorContainer
                            isEnd -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val bg by animateColorAsState(targetBg, label = "favoritesSwipeBg")

                        val targetTint = when {
                            isStart -> MaterialTheme.colorScheme.onErrorContainer
                            isEnd -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val tint by animateColorAsState(targetTint, label = "favoritesSwipeTint")

                        val iconScale by animateFloatAsState(
                            targetValue = if (isDefault) 0.85f else 1.15f,
                            label = "favoritesIconScale"
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
                            headlineContent = { Text(fav.word) },
                            supportingContent = { Text(fav.definition) },
                            modifier = Modifier.clickable { onOpenDetail(fav.word) }
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
