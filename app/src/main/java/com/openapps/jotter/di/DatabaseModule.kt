package com.openapps.jotter.di

import android.content.Context
import androidx.room.Room
import com.openapps.jotter.data.repository.NotesRepository
import com.openapps.jotter.data.repository.NotesRepositoryImpl
import com.openapps.jotter.data.source.JotterDatabase
import com.openapps.jotter.data.source.NoteDao
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

    // --- BINDINGS (Interface to Implementation) ---
    // Tells Hilt: When someone asks for the NotesRepository interface, give them NotesRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        notesRepositoryImpl: NotesRepositoryImpl
    ): NotesRepository


    companion object {
        // --- PROVIDERS (Concrete Objects) ---

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): JotterDatabase {
            return Room.databaseBuilder(
                context,
                JotterDatabase::class.java,
                "jotter_db" // Database file name
            ).build()
        }

        @Provides
        fun provideNoteDao(database: JotterDatabase): NoteDao {
            return database.noteDao()
        }
    }
}