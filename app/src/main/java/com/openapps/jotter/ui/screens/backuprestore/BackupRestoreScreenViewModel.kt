package com.openapps.jotter.ui.screens.backuprestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.openapps.jotter.data.model.BackupData
import com.openapps.jotter.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * UI state for Backup & Restore screen.
 */
data class BackupRestoreUiState(
    val isExportInProgress: Boolean = false,
    val isImportInProgress: Boolean = false,
    val lastExportSuccess: Boolean? = null,
    val lastImportSuccess: Boolean? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BackupRestoreScreenViewModel @Inject constructor(
    private val repository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    /**
     * Generates the Backup JSON string.
     * The UI calls this, and when the JSON is ready, the [onReady] callback is fired
     * so the UI can write it to the file system (using ContentResolver).
     */
    fun exportNotes(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Start Loading
            _uiState.update { it.copy(isExportInProgress = true, errorMessage = null, lastExportSuccess = null) }

            try {
                // 2. Fetch Data
                val (notes, categories) = repository.getBackupData()
                val backupData = BackupData(notes, categories)

                // 3. Convert to JSON
                val jsonString = gson.toJson(backupData)

                // 4. Update State & Callback
                _uiState.update { it.copy(isExportInProgress = false, lastExportSuccess = true) }

                withContext(Dispatchers.Main) {
                    onReady(jsonString)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(isExportInProgress = false, lastExportSuccess = false, errorMessage = e.localizedMessage)
                }
            }
        }
    }

    /**
     * Reads the file stream, parses JSON, and restores to Database.
     */
    fun importNotes(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Start Loading
            _uiState.update { it.copy(isImportInProgress = true, errorMessage = null, lastImportSuccess = null) }

            try {
                // 2. Read & Parse
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()
                val backupData = gson.fromJson(jsonString, BackupData::class.java)

                // 3. Restore to DB
                repository.restoreBackupData(backupData.notes, backupData.categories)

                // 4. Success
                _uiState.update { it.copy(isImportInProgress = false, lastImportSuccess = true) }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(isImportInProgress = false, lastImportSuccess = false, errorMessage = "Invalid Backup File")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, lastExportSuccess = null, lastImportSuccess = null) }
    }
}