package com.neologotron.app.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.repo.OnboardingRepository

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val repo: OnboardingRepository,
    ) : ViewModel() {
        suspend fun checkComplete(): Boolean = repo.isComplete.first()

        fun markComplete() {
            viewModelScope.launch { repo.setComplete() }
        }
    }
