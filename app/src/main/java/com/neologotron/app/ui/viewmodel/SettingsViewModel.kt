package com.neologotron.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.neologotron.app.domain.GeneratorOptionsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val options: GeneratorOptionsStore
) : ViewModel() {
    fun resetTags() = options.clear()
}
