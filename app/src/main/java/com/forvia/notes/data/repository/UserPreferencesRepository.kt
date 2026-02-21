/*
 * Copyright (c) 2026 Forvia
 *
 * This file is part of Notes
 *
 * Notes is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Notes is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Notes.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.forvia.notes.data.repository

import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
@Immutable
data class UserPreferences(
    val isTrueBlackEnabled: Boolean = false,
    val isHapticEnabled: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val dateFormat: String = "dd MMM"
)

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val IS_TRUE_BLACK = booleanPreferencesKey("is_true_black")
        val IS_HAPTIC = booleanPreferencesKey("is_haptic")
        val IS_BIOMETRIC = booleanPreferencesKey("is_biometric")
        val IS_APP_LOCK = booleanPreferencesKey("is_app_lock")
        val DATE_FORMAT = stringPreferencesKey("date_format")
    }
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                isTrueBlackEnabled = preferences[Keys.IS_TRUE_BLACK] ?: false,
                isHapticEnabled = preferences[Keys.IS_HAPTIC] ?: true,
                isBiometricEnabled = preferences[Keys.IS_BIOMETRIC] ?: false,
                isAppLockEnabled = preferences[Keys.IS_APP_LOCK] ?: false,
                dateFormat = preferences[Keys.DATE_FORMAT] ?: "dd MMM"
            )
        }
        .distinctUntilChanged()

    suspend fun setTrueBlack(enabled: Boolean) {
        dataStore.edit { it[Keys.IS_TRUE_BLACK] = enabled }
    }

    suspend fun setHaptic(enabled: Boolean) {
        dataStore.edit { it[Keys.IS_HAPTIC] = enabled }
    }

    suspend fun setBiometric(enabled: Boolean) {
        dataStore.edit { it[Keys.IS_BIOMETRIC] = enabled }
    }

    suspend fun setAppLock(enabled: Boolean) {
        dataStore.edit { it[Keys.IS_APP_LOCK] = enabled }
    }

    suspend fun clearAllData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun setDateFormat(format: String) {
        dataStore.edit { it[Keys.DATE_FORMAT] = format }
    }
}