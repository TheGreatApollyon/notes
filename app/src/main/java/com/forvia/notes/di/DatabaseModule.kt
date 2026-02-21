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

package com.forvia.notes.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.forvia.notes.data.repository.CategoryRepository
import com.forvia.notes.data.repository.CategoryRepositoryImpl
import com.forvia.notes.data.repository.NotesRepository
import com.forvia.notes.data.repository.NotesRepositoryImpl
import com.forvia.notes.data.source.CategoryDao
import com.forvia.notes.data.source.NotesDatabase
import com.forvia.notes.data.source.NoteDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        notesRepositoryImpl: NotesRepositoryImpl
    ): NotesRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository


    companion object {
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_isArchived_isTrashed` ON `notes` (`isArchived`, `isTrashed`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_category` ON `notes` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_isPinned_updatedTime` ON `notes` (`isPinned`, `updatedTime`)")
            }
        }

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): NotesDatabase {
            return Room.databaseBuilder(
                context,
                NotesDatabase::class.java,
                "notes_db"
            )
                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        @Provides
        fun provideNoteDao(database: NotesDatabase): NoteDao {
            return database.noteDao()
        }

        @Provides
        fun provideCategoryDao(database: NotesDatabase): CategoryDao {
            return database.categoryDao()
        }
    }
}