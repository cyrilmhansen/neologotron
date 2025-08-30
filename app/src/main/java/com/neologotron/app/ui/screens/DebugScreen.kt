package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import com.neologotron.app.R
import com.neologotron.app.ui.viewmodel.DebugViewModel

@Composable
fun DebugScreen(
    vm: DebugViewModel = hiltViewModel(),
    onShowMessage: (String) -> Unit = {},
) {
    val buildTime by vm.dbBuildTimeText.collectAsState()
    val resetting by vm.resetting.collectAsState()
    val scope = rememberCoroutineScope()
    var onboardingDisabled by remember { mutableStateOf(false) }
    val onboardingResetMsg = stringResource(id = R.string.msg_onboarding_reset)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = stringResource(id = R.string.title_debug), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(id = R.string.label_db_build_time))
        Text(text = buildTime, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { vm.resetDb() }, enabled = !resetting) {
            Text(text = stringResource(id = R.string.action_reset_db))
        }
        if (resetting) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(id = R.string.label_resetting))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                vm.resetOnboarding()
                onShowMessage(onboardingResetMsg)
                scope.launch {
                    onboardingDisabled = true
                    delay(1200)
                    onboardingDisabled = false
                }
            },
            enabled = !onboardingDisabled,
        ) {
            Text(text = stringResource(id = R.string.action_reset_onboarding))
        }
    }
}
