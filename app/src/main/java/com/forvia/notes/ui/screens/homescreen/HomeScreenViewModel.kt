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

package com.forvia.notes.ui.screens.homescreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forvia.notes.data.model.Note
import com.forvia.notes.data.repository.CategoryRepository
import com.forvia.notes.data.repository.NotesRepository
import com.forvia.notes.data.repository.UserPreferences
import com.forvia.notes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _selectedCategory = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")
    private val _isGridView = MutableStateFlow(true)

    private val notesFlow = notesRepository.getAllNotes()
    private val categoriesFlow = categoryRepository.getAllCategories().map { list -> list.map { it.name } }
    private val prefsFlow = userPreferencesRepository.userPreferencesFlow

    val uiState: StateFlow<UiState> = combine(
        combine(notesFlow, categoriesFlow, prefsFlow) { notes, categories, prefs ->
            Triple(notes, categories, prefs)
        },
        _selectedCategory,
        _searchQuery,
        _isGridView
    ) { (notes, categories, prefs), selectedCategory, searchQuery, isGridView ->
        val validatedCategory = if (selectedCategory != "All" && 
            !listOf("Pinned", "Locked").contains(selectedCategory) && 
            !categories.contains(selectedCategory)) {
            "All"
        } else {
            selectedCategory
        }

        val filteredNotes = if (searchQuery.isNotBlank()) {
            notes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        } else {
            when (validatedCategory) {
                "All"     -> notes
                "Pinned"  -> notes.filter { it.isPinned }
                "Locked"  -> notes.filter { it.isLocked }
                else      -> notes.filter { it.category == validatedCategory }
            }
        }

        UiState(
            allNotes = filteredNotes,
            selectedCategory = validatedCategory,
            searchQuery = searchQuery,
            isGridView = isGridView,
            allAvailableCategories = categories,
            isBiometricEnabled = prefs.isBiometricEnabled,
            dateFormat = prefs.dateFormat
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
        val allNotes: List<Note> = emptyList(),
        val selectedCategory: String = "All",
        val searchQuery: String = "",
        val isGridView: Boolean = true,
        val allAvailableCategories: List<String> = emptyList(),
        val isBiometricEnabled: Boolean = false,
        val dateFormat: String = "dd MMM"
    )

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onNoteClicked(noteId: Int) {
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            val updatedNote = note.copy(isPinned = !note.isPinned)
            notesRepository.updateNote(updatedNote)
        }
    }

    fun toggleLock(note: Note) {
        viewModelScope.launch {
            val updatedNote = note.copy(isLocked = !note.isLocked)
            notesRepository.updateNote(updatedNote)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            notesRepository.archiveNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            notesRepository.trashNote(note)
        }
    }
}
