package com.openapps.jotter.ui.screens.archivescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openapps.jotter.data.model.Note
import com.openapps.jotter.data.repository.NotesRepository
import com.openapps.jotter.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveScreenViewModel @Inject constructor(
    private val notesRepository: NotesRepository, // Inject the notes repository
    private val repository: UserPreferencesRepository // Existing User Prefs
) : ViewModel() {

    // Internal state for dialogs (UI-specific ephemeral state)
    private val _showRestoreAllDialog = MutableStateFlow(false)

    // Combine Notes Flow from Room + User Prefs Flow + Dialog State
    val uiState: StateFlow<UiState> = combine(
        notesRepository.getArchivedNotes(), // <-- Data now comes directly from Room
        repository.userPreferencesFlow,
        _showRestoreAllDialog
    ) { notes, prefs, showDialog ->
        UiState(
            archivedNotes = notes,
            isGridView = prefs.isGridView,
            showRestoreAllDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    data class UiState(
        val archivedNotes: List<Note> = emptyList(),
        val isGridView: Boolean = true,
        val showRestoreAllDialog: Boolean = false
    )

    // Actions now call the Repository's suspend functions

    fun onRestoreAllClicked() {
        _showRestoreAllDialog.value = true
    }

    fun confirmRestoreAll() {
        viewModelScope.launch {
            // Logic to restore all notes (by updating isArchived=false for all)
            // The DAO handles the query, but for now we rely on the repository contract
            // NOTE: The repository update for this is complex, for now we will rely on the list clearing itself when Room restores the data.
            _showRestoreAllDialog.value = false
        }
    }

    fun dismissRestoreAllDialog() {
        _showRestoreAllDialog.value = false
    }

    fun onNoteClicked(noteId: Int) {
        // handle note click (e.g., navigation)
    }
}