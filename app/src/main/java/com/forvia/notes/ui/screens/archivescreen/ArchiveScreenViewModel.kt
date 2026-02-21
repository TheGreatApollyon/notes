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

package com.forvia.notes.ui.screens.archivescreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forvia.notes.data.model.Note
import com.forvia.notes.data.repository.NotesRepository
import com.forvia.notes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveScreenViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _showRestoreAllDialog = MutableStateFlow(false)
    private val _showBulkActionDialog = MutableStateFlow(false)
    private val _isGridView = MutableStateFlow(true)

    val uiState: StateFlow<UiState> = combine(
        notesRepository.getArchivedNotes(),
        _showRestoreAllDialog,
        _showBulkActionDialog,
        _isGridView
    ) { notes, showRestore, showBulk, isGridView ->
        UiState(
            archivedNotes = notes,
            isGridView = isGridView,
            showRestoreAllDialog = showRestore,
            showBulkActionDialog = showBulk
        )
    }
    .distinctUntilChanged()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    @Immutable
    data class UiState(
        val archivedNotes: List<Note> = emptyList(),
        val isGridView: Boolean = true,
        val showRestoreAllDialog: Boolean = false,
        val showBulkActionDialog: Boolean = false
    )

    fun onRestoreAllClicked() {
        _showRestoreAllDialog.value = true
    }

    fun confirmRestoreAll() {
        viewModelScope.launch {
            val archivedNotes = notesRepository.getArchivedNotes().first()
            archivedNotes.forEach { note ->
                notesRepository.unarchiveNote(note)
            }
            _showRestoreAllDialog.value = false
        }
    }

    fun dismissRestoreAllDialog() {
        _showRestoreAllDialog.value = false
    }

    fun onBulkActionClicked() {
        _showBulkActionDialog.value = true
    }

    fun dismissBulkActionDialog() {
        _showBulkActionDialog.value = false
    }

    fun confirmMoveAllToTrash() {
        viewModelScope.launch {
            val archivedNotes = notesRepository.getArchivedNotes().first()
            archivedNotes.forEach { note ->
                notesRepository.trashNote(note)
            }
            _showBulkActionDialog.value = false
        }
    }

    fun onNoteClicked(noteId: Int) {
    }
}