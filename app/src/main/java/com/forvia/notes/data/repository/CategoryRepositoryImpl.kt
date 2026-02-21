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

import androidx.room.withTransaction
import com.forvia.notes.data.model.Category
import com.forvia.notes.data.source.CategoryDao
import com.forvia.notes.data.source.NotesDatabase
import com.forvia.notes.data.source.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val noteDao: NoteDao,
    private val database: NotesDatabase
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    override suspend fun insertCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            categoryDao.insertCategory(Category(name = trimmed))
        }
    }

    override suspend fun renameCategory(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank() && oldName != trimmed) {
            database.withTransaction {
                categoryDao.updateCategoryName(oldName, trimmed)
                noteDao.updateNoteCategories(oldName, trimmed)
            }
        }
    }

    override suspend fun deleteCategoryByName(name: String) {
        categoryDao.deleteCategoryByName(name)
    }

    override suspend fun clearCategoryReferences(categoryName: String) {
        noteDao.updateNoteCategories(categoryName, "")
    }
}