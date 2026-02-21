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

package com.forvia.notes.ui.screens.settingsscreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forvia.notes.data.repository.NotesRepository
import com.forvia.notes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val notesRepository: NotesRepository
) : ViewModel() {
    val uiState: StateFlow<UiState> = repository.userPreferencesFlow
        .map { prefs ->
            UiState(
                isLoading = false,
                isTrueBlackEnabled = prefs.isTrueBlackEnabled,
                isHapticEnabled = prefs.isHapticEnabled,
                isBiometricEnabled = prefs.isBiometricEnabled,
                isAppLockEnabled = prefs.isAppLockEnabled,
                dateFormat = prefs.dateFormat
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(isLoading = true)
        )

    @Immutable
    data class UiState(
        val isLoading: Boolean = true,
        val isTrueBlackEnabled: Boolean = false,
        val isHapticEnabled: Boolean = true,
        val isBiometricEnabled: Boolean = false,
        val isAppLockEnabled: Boolean = false,
        val dateFormat: String = "dd MMM",
    )

    fun updateTrueBlackMode(isEnabled: Boolean) {
        viewModelScope.launch { repository.setTrueBlack(isEnabled) }
    }

    fun updateHapticEnabled(isEnabled: Boolean) {
        viewModelScope.launch { repository.setHaptic(isEnabled) }
    }

    fun updateBiometricEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometric(isEnabled)
            if (!isEnabled) {
                notesRepository.unlockAllNotes()
            }
        }
    }

    fun updateAppLockEnabled(isEnabled: Boolean) {
        viewModelScope.launch { repository.setAppLock(isEnabled) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            notesRepository.clearAllDatabaseData()
            repository.clearAllData()
        }
    }

    fun updateDateFormat(format: String) {
        viewModelScope.launch { repository.setDateFormat(format) }
    }
}