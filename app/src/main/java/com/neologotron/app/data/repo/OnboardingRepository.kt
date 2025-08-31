package com.neologotron.app.data.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class OnboardingRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val key = booleanPreferencesKey("onboarding_complete")

        val isComplete: Flow<Boolean> =
            context.dataStore.data.map { prefs ->
                prefs[key] ?: false
            }

        suspend fun setComplete() {
            context.dataStore.edit { prefs ->
                prefs[key] = true
            }
        }

        suspend fun reset() {
            context.dataStore.edit { prefs ->
                prefs[key] = false
            }
        }
    }
