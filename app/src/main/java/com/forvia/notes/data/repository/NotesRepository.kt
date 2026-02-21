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

import com.forvia.notes.data.model.BackupData
import com.forvia.notes.data.model.Note
import kotlinx.coroutines.flow.Flow
interface NotesRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    fun getTrashedNotes(): Flow<List<Note>>
    fun getCategories(): Flow<List<String>>
    suspend fun getNoteById(noteId: Int): Note?
    suspend fun addNote(note: Note): Long
    suspend fun updateNote(note: Note)

    suspend fun archiveNote(note: Note)
    suspend fun unarchiveNote(note: Note)
    suspend fun trashNote(note: Note)
    suspend fun restoreNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun emptyTrash()

    suspend fun getBackupData(): BackupData
    suspend fun restoreBackupData(backupData: BackupData)

    suspend fun clearAllDatabaseData()
    suspend fun unlockAllNotes()
}