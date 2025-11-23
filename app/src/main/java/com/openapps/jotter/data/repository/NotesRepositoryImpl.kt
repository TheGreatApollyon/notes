package com.openapps.jotter.data.repository

import com.openapps.jotter.data.model.Category // ✨ Add Import
import com.openapps.jotter.data.model.Note
import com.openapps.jotter.data.source.CategoryDao // ✨ Add Import
import com.openapps.jotter.data.source.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Concrete implementation of the NotesRepository interface, using the local Room DAO.
 */
class NotesRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao, // ✨ Added missing comma here
    private val categoryDao: CategoryDao
) : NotesRepository {

    // --- Read Operations ---
    override fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    override fun getArchivedNotes(): Flow<List<Note>> = noteDao.getArchivedNotes()
    override fun getTrashedNotes(): Flow<List<Note>> = noteDao.getTrashedNotes()

    override suspend fun getNoteById(noteId: Int): Note? = noteDao.getNoteById(noteId)

    // --- Write/Update Operations ---
    override suspend fun addNote(note: Note): Long {
        // Automatically updates 'updatedTime' whenever a note is added/saved
        return noteDao.insert(note.copy(updatedTime = System.currentTimeMillis()))
    }

    override suspend fun updateNote(note: Note) {
        // Automatically updates 'updatedTime' whenever a note is updated
        noteDao.update(note.copy(updatedTime = System.currentTimeMillis()))
    }

    // Helper functions for status changes
    override suspend fun archiveNote(note: Note) {
        // Update flags: set archived=true, set updated time
        noteDao.update(note.copy(
            isArchived = true,
            isTrashed = false,
            updatedTime = System.currentTimeMillis()
        ))
    }

    override suspend fun unarchiveNote(note: Note) {
        // Update flags: set archived=false, set updated time
        noteDao.update(note.copy(
            isArchived = false,
            isTrashed = false,
            updatedTime = System.currentTimeMillis()
        ))
    }

    override suspend fun trashNote(note: Note) {
        // Update flags: set trashed=true, set updated time
        noteDao.update(note.copy(
            isTrashed = true,
            isArchived = false, // Must ensure it's not archived when in trash
            updatedTime = System.currentTimeMillis()
        ))
    }

    override suspend fun restoreNote(note: Note) {
        // Update flags: set both to false (restores to active notes)
        noteDao.update(note.copy(
            isTrashed = false,
            isArchived = false,
            updatedTime = System.currentTimeMillis()
        ))
    }

    // --- Delete Operations ---
    override suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }

    override suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }

    override fun getCategories(): Flow<List<String>> = noteDao.getCategories()

    // --- Backup & Restore Implementation ---

    override suspend fun getBackupData(): Pair<List<Note>, List<Category>> {
        val notes = noteDao.getAllNotesSync()
        val categories = categoryDao.getAllCategoriesSync()
        return Pair(notes, categories)
    }

    override suspend fun restoreBackupData(notes: List<Note>, categories: List<Category>) {
        // 1. Wipe existing data
        noteDao.deleteAllNotes()
        categoryDao.deleteAllCategories()

        // 2. Insert backup data
        noteDao.insertAll(notes)
        categoryDao.insertAll(categories)
    }
}