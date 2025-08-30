package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neologotron.app.data.db.AppDatabase
import com.neologotron.app.data.repo.AdminRepository
import com.neologotron.app.data.repo.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val db: AppDatabase,
    private val admin: AdminRepository,
    private val onboarding: OnboardingRepository,
) : ViewModel() {
    private val _dbBuildTimeText = MutableStateFlow("—")
    val dbBuildTimeText: StateFlow<String> = _dbBuildTimeText

    private val _resetting = MutableStateFlow(false)
    val resetting: StateFlow<Boolean> = _resetting

    init {
        refreshMeta()
    }

    fun refreshMeta() {
        viewModelScope.launch {
            val meta = db.metaDao().get()
            _dbBuildTimeText.value = meta?.let {
                DateFormat.getDateTimeInstance().format(Date(it.createdAtMillis))
            } ?: "(inconnu)"
        }
    }

    fun resetDb() {
        viewModelScope.launch {
            _resetting.value = true
            runCatching { admin.resetAndReseed() }
                .onSuccess { refreshMeta() }
                .also { _resetting.value = false }
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            runCatching { onboarding.reset() }
        }
    }
}
