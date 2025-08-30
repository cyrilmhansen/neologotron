package com.neologotron.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neologotron.app.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingCombineScreen(onNext: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Combiner")
        Button(onClick = onNext) { Text("Suivant") }
        Button(onClick = onSkip) { Text("Passer") }
    }
}

@Composable
fun OnboardingCreateScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Créer")
        Button(onClick = onNext) { Text("Suivant") }
    }
}

@Composable
fun OnboardingShareScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Partager")
        Button(onClick = onFinish) { Text("Commencer") }
    }
}

@Composable
fun OnboardingFlow(onFinished: () -> Unit) {
    val nav = rememberNavController()
    val vm: OnboardingViewModel = hiltViewModel()
    NavHost(navController = nav, startDestination = "combine") {
        composable("combine") {
            OnboardingCombineScreen(
                onNext = { nav.navigate("create") },
                onSkip = {
                    vm.markComplete()
                    onFinished()
                }
            )
        }
        composable("create") {
            OnboardingCreateScreen(onNext = { nav.navigate("share") })
        }
        composable("share") {
            OnboardingShareScreen(onFinish = {
                vm.markComplete()
                onFinished()
            })
        }
    }
}

