package com.neologotron.app.ui

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Data<T>(val value: T) : UiState<T>()
    data class Error(val message: String? = null) : UiState<Nothing>()
}

