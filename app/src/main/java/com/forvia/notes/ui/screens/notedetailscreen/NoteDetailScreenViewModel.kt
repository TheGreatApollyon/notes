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

package com.forvia.notes.ui.screens.notedetailscreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.forvia.notes.data.model.Note
import com.forvia.notes.data.repository.CategoryRepository
import com.forvia.notes.data.repository.NotesRepository
import com.forvia.notes.data.repository.UserPreferences
import com.forvia.notes.data.repository.UserPreferencesRepository
import com.forvia.notes.navigation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notesRepository: NotesRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AppRoutes.NoteDetail>()
    private val noteId = route.noteId
    private val passedCategory = route.category

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, UserPreferences())

    @Immutable
    data class UiState(
        val id: Int? = null,
        val title: String = "",
        val content: String = "",
        val category: String = "",
        val isPinned: Boolean = false,
        val isLocked: Boolean = false,
        val isArchived: Boolean = false,
        val isTrashed: Boolean = false,
        val createdTime: Long = System.currentTimeMillis(),
        val lastEdited: Long = System.currentTimeMillis(),
        val isNotePersisted: Boolean = false,
        val isLoading: Boolean = true,
        val isModified: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var originalState: UiState? = null

    val availableCategories: StateFlow<List<String>> = categoryRepository.getAllCategories()
        .map { categoryList -> categoryList.map { it.name } }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        if (noteId != -1) {
            loadNote(noteId)
        } else {
            val initialState = UiState(
                category = passedCategory ?: "",
                isNotePersisted = false, 
                isLoading = false
            )
            _uiState.update { initialState }
            originalState = initialState
        }
        observeCategoryCleanup()
        observeNoteUpdates()
    }

    private fun observeCategoryCleanup() {
        viewModelScope.launch {
            availableCategories.drop(1).collectLatest { categories ->
                val currentCategory = uiState.value.category
                if (currentCategory.isNotBlank() && !categories.contains(currentCategory)) {
                    _uiState.update { it.copy(category = "") }
                    checkForChanges()
                }
            }
        }
    }

    private fun observeNoteUpdates() {
        if (noteId != -1) {
            viewModelScope.launch {
                notesRepository.getAllNotes().collectLatest { notes ->
                    val updatedNote = notes.find { it.id == noteId }
                    if (updatedNote != null && updatedNote.category != uiState.value.category) {
                        _uiState.update { it.copy(category = updatedNote.category) }
                        originalState = originalState?.copy(category = updatedNote.category)
                        checkForChanges()
                    }
                }
            }
        }
    }

    private fun loadNote(id: Int) {
        viewModelScope.launch {
            val note = notesRepository.getNoteById(id)
            if (note != null) {
                val newState = UiState(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    category = note.category,
                    isPinned = note.isPinned,
                    isLocked = note.isLocked,
                    isArchived = note.isArchived,
                    isTrashed = note.isTrashed,
                    createdTime = note.createdTime,
                    lastEdited = note.updatedTime,
                    isNotePersisted = true,
                    isLoading = false,
                    isModified = false
                )
                _uiState.update { newState }
                originalState = newState
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun saveNoteStatus() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState.id != null) {
                val updatedNote = Note(
                    id = currentState.id,
                    title = currentState.title,
                    content = currentState.content,
                    category = currentState.category,
                    isPinned = currentState.isPinned,
                    isLocked = currentState.isLocked,
                    isArchived = currentState.isArchived,
                    isTrashed = currentState.isTrashed,
                    createdTime = currentState.createdTime,
                    updatedTime = System.currentTimeMillis()
                )
                notesRepository.updateNote(updatedNote)
            }
        }
    }

    private fun checkForChanges() {
        val current = _uiState.value
        val original = originalState ?: return
        val modified = current.title != original.title ||
                       current.content != original.content ||
                       current.category != original.category
        
        if (current.isModified != modified) {
            _uiState.update { it.copy(isModified = modified) }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        checkForChanges()
    }

    fun updateContent(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
        checkForChanges()
    }

    fun updateCategory(newCategory: String) {
        _uiState.update { it.copy(category = newCategory) }
        checkForChanges()
    }

    fun createAndSelectCategory(categoryName: String) {
        viewModelScope.launch {
            categoryRepository.insertCategory(categoryName)
            _uiState.update { it.copy(category = categoryName) }
            checkForChanges()
        }
    }

    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
        saveNoteStatus()
    }

    fun toggleLock() {
        _uiState.update { it.copy(isLocked = !it.isLocked) }
        saveNoteStatus()
    }

    @OptIn(FlowPreview::class)
    fun autoSave() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.title.isBlank() && currentState.content.isBlank()) {
                return@launch
            }

            if (currentState.category.isNotBlank()) {
                categoryRepository.insertCategory(currentState.category)
            }

            val noteToSave = Note(
                id = currentState.id ?: 0,
                title = currentState.title,
                content = currentState.content,
                category = currentState.category,
                isPinned = currentState.isPinned,
                isLocked = currentState.isLocked,
                isArchived = currentState.isArchived,
                isTrashed = currentState.isTrashed,
                createdTime = currentState.createdTime,
                updatedTime = System.currentTimeMillis()
            )

            if (currentState.isNotePersisted && currentState.id != null) {
                notesRepository.updateNote(noteToSave)
            } else {
                val newId = notesRepository.addNote(noteToSave).toInt()
                _uiState.update { 
                    it.copy(
                        id = newId,
                        isNotePersisted = true
                    )
                }
                originalState = _uiState.value
            }
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.category.isNotBlank()) {
                categoryRepository.insertCategory(currentState.category)
            }

            val noteToSave = Note(
                id = currentState.id ?: 0,
                title = currentState.title,
                content = currentState.content,
                category = currentState.category,
                isPinned = currentState.isPinned,
                isLocked = currentState.isLocked,
                isArchived = currentState.isArchived,
                isTrashed = currentState.isTrashed
            )

            if (currentState.isNotePersisted) {
                notesRepository.updateNote(noteToSave)
                loadNote(currentState.id!!)
            } else {
                val newId = notesRepository.addNote(noteToSave).toInt()
                loadNote(newId)
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            val note = uiState.value
            val noteToDelete = Note(id = note.id ?: 0)
            if (note.isTrashed) {
                notesRepository.deleteNote(noteToDelete)
            } else {
                notesRepository.trashNote(noteToDelete)
            }
        }
    }

    fun archiveNote() {
        viewModelScope.launch {
            notesRepository.archiveNote(Note(id = uiState.value.id ?: 0))
        }
    }

    fun restoreNote() {
        viewModelScope.launch {
            notesRepository.restoreNote(Note(id = uiState.value.id ?: 0))
        }
    }

    fun undoChanges() {
        if (noteId != -1) {
            loadNote(noteId)
        }
    }
}