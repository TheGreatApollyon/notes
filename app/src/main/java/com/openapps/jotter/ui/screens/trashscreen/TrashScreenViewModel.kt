package com.openapps.jotter.ui.screens.trashscreen

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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashScreenViewModel @Inject constructor(
    private val notesRepository: NotesRepository, // Inject the notes repository
    private val userPreferencesRepository: UserPreferencesRepository // Existing User Prefs
) : ViewModel() {

    // Internal state for dialogs (UI-specific ephemeral state)
    private val _showEmptyTrashDialog = MutableStateFlow(false)

    // Combine Notes Flow from Room + User Prefs Flow + Dialog State
    val uiState: StateFlow<UiState> = combine(
        notesRepository.getTrashedNotes(), // <-- Data now comes directly from Room
        userPreferencesRepository.userPreferencesFlow,
        _showEmptyTrashDialog
    ) { notes, prefs, showDialog ->
        UiState(
            trashedNotes = notes,
            isGridView = prefs.isGridView,
            showEmptyTrashDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    data class UiState(
        val trashedNotes: List<Note> = emptyList(),
        val isGridView: Boolean = true,
        val showEmptyTrashDialog: Boolean = false
    )

    // Actions now call the Repository's suspend functions

    fun onEmptyTrashClicked() {
        _showEmptyTrashDialog.value = true
    }

    fun confirmEmptyTrash() {
        viewModelScope.launch {
            notesRepository.emptyTrash() // Call the repository function
            _showEmptyTrashDialog.value = false
        }
    }

    fun dismissEmptyTrashDialog() {
        _showEmptyTrashDialog.value = false
    }

    fun onNoteClicked(noteId: Int) {
        // handle note click (e.g., navigation)
    }
}