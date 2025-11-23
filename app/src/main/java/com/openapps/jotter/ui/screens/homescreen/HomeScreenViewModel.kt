package com.openapps.jotter.ui.screens.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openapps.jotter.data.model.Note // Use the new Note Entity
import com.openapps.jotter.data.repository.NotesRepository // Inject the new Notes Repository
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
class HomeScreenViewModel @Inject constructor(
    // 1. Inject both Repositories
    private val notesRepository: NotesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Internal flow for category selection (remains local)
    private val _categoryFlow = MutableStateFlow("All")

    // 1. Reactive UI State
    // Combines Notes Flow from Room + User Prefs Flow from DataStore + Local Category
    val uiState: StateFlow<UiState> = combine(
        notesRepository.getAllNotes(), // <-- Data now comes directly from Room/Repository
        userPreferencesRepository.userPreferencesFlow,
        _categoryFlow
    ) { notes, prefs, category ->

        // --- Filtering Logic (Moved to ViewModel) ---
        val filteredNotes = when (category) {
            "All"     -> notes
            "Pinned"  -> notes.filter { it.isPinned }
            "Locked"  -> notes.filter { it.isLocked }
            else      -> notes.filter { it.category == category }
        }

        UiState(
            allNotes = filteredNotes, // Filtered list based on category
            selectedCategory = category,
            isGridView = prefs.isGridView // Controlled by DataStore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    data class UiState(
        val allNotes: List<Note> = emptyList(),
        val selectedCategory: String = "All",
        val isGridView: Boolean = true
    )

    // 2. Actions

    fun toggleGridView() {
        // Reads current value from state and flips it via DataStore
        val currentIsGrid = uiState.value.isGridView
        viewModelScope.launch {
            userPreferencesRepository.setGridView(!currentIsGrid)
        }
    }

    fun selectCategory(category: String) {
        _categoryFlow.value = category
    }

    fun onNoteClicked(noteId: Int) { }
    fun onAddNoteClick() { }
    fun onAddCategoryClick() { }
    fun onSettingsClick() { }
}