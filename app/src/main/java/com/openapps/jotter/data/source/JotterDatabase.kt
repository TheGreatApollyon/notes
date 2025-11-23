package com.openapps.jotter.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openapps.jotter.data.model.Note

/**
 * Defines the main access point for the Room database.
 */
@Database(
    entities = [Note::class], // List all entities here
    version = 1,
    exportSchema = false
)
abstract class JotterDatabase : RoomDatabase() {

    // Abstract function to get the DAO. Room generates the implementation.
    abstract fun noteDao(): NoteDao
}