package com.openapps.jotter.ui.screens.notedetailscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openapps.jotter.data.model.Note // Import the Room Entity
import com.openapps.jotter.data.repository.NotesRepository // Inject the Notes Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notesRepository: NotesRepository // 1. Inject the Notes Repository
) : ViewModel() {

    // 1. Get Note ID from Navigation Arguments
    private val noteId: Int? = savedStateHandle.get<Int>("noteId")

    // 2. UI State Definition
    data class UiState(
        val id: Int? = null,
        val title: String = "",
        val content: String = "",
        val category: String = "Uncategorized",
        val isPinned: Boolean = false,
        val isLocked: Boolean = false,
        val isArchived: Boolean = false,
        val isTrashed: Boolean = false,
        val lastEdited: Long = System.currentTimeMillis(),
        val isNotePersisted: Boolean = false,
        val isLoading: Boolean = true // Added state for loading check
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        if (noteId != null && noteId != -1) {
            loadNote(noteId)
        } else {
            // New Note State - Still loading preferences, but note is not persisted
            _uiState.update { it.copy(isNotePersisted = false, isLoading = false) }
        }
    }

    private fun loadNote(id: Int) {
        viewModelScope.launch {
            // Fetch note from Room Repository
            val note = notesRepository.getNoteById(id)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        category = note.category,
                        isPinned = note.isPinned,
                        isLocked = note.isLocked,
                        isArchived = note.isArchived,
                        isTrashed = note.isTrashed,
                        lastEdited = note.updatedTime,
                        isNotePersisted = true,
                        isLoading = false // Loading complete
                    )
                }
            } else {
                // Note not found (e.g., deleted), reset state
                _uiState.value = UiState(isLoading = false)
            }
        }
    }

    // Helper function to save status changes (Pin/Lock) without triggering a full content save
    // Helper function to save status changes (Pin/Lock) without triggering a full content save
    private fun saveNoteStatus() {
        viewModelScope.launch {
            val currentState = uiState.value
            // Fix: Check if ID exists before proceeding
            if (currentState.id != null) {
                val updatedNote = Note(
                    // FIX: Use '!!' because the 'if' statement guarantees it is non-null
                    id = currentState.id!!,
                    title = currentState.title,
                    content = currentState.content,
                    category = currentState.category,
                    isPinned = currentState.isPinned,
                    isLocked = currentState.isLocked,
                    isArchived = currentState.isArchived,
                    isTrashed = currentState.isTrashed,
                    updatedTime = currentState.lastEdited // Repository updates time
                )
                notesRepository.updateNote(updatedNote)
            }
        }
    }

    // --- User Actions ---

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateContent(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
    }

    fun updateCategory(newCategory: String) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
        saveNoteStatus() // Save status immediately
    }

    fun toggleLock() {
        _uiState.update { it.copy(isLocked = !it.isLocked) }
        saveNoteStatus() // Save status immediately
    }

    fun saveNote() {
        viewModelScope.launch {
            val currentState = _uiState.value

            val noteToSave = Note(
                id = currentState.id ?: 0,
                // ... (other properties)
            )

            // newId is inferred as Long? by addNote() or Int? by type casting.
            // It must be Int? because you are using it in a context where it could be null
            // (the original currentState.id is Int?).
            val potentiallyNullableId = if (currentState.isNotePersisted) {
                notesRepository.updateNote(noteToSave)
                currentState.id
            } else {
                notesRepository.addNote(noteToSave).toInt()
            }

            // 🐛 FIX IS HERE: Ensure the ID is non-nullable before passing it to getNoteById()
            potentiallyNullableId?.let { freshNoteId ->
                notesRepository.getNoteById(freshNoteId)?.let { freshNote ->
                    _uiState.update {
                        it.copy(
                            id = freshNote.id,
                            isNotePersisted = true,
                            lastEdited = freshNote.updatedTime
                        )
                    }
                }
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            val note = uiState.value
            val noteToDelete = Note(id = note.id ?: 0)

            if (note.isTrashed) {
                // Permanently delete if already in trash
                notesRepository.deleteNote(noteToDelete)
            } else {
                // Move to trash (update status)
                notesRepository.trashNote(noteToDelete)
            }
        }
    }

    fun restoreNote() {
        viewModelScope.launch {
            val note = uiState.value
            notesRepository.restoreNote(
                Note(id = note.id ?: 0)
            )
            // The DAO flow will trigger the UI to update; no need to manually update flags
        }
    }

    fun undoChanges() {
        // If it's an existing note, reload the original data from Room
        if (noteId != null && noteId != -1) {
            loadNote(noteId)
        }
    }
}