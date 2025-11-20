package com.openapps.jotter.ui.screens.homescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.openapps.jotter.components.FAB
import com.openapps.jotter.components.Header
// Make sure this matches where you saved the new CategoryBar file
import com.openapps.jotter.ui.components.CategoryBar

@Composable
fun HomeScreen() {
    // State tracks the active category
    var selectedCategory by remember { mutableStateOf("All") }

    // Raw categories (The component automatically adds "All" to the start)
    val categories = listOf(
        "Personal", "Work", "Ideas", "To-do",
        "Shopping", "Finance", "Health", "Movies",
        "Books", "Coding", "Travel", "Random"
    )

    Scaffold(
        topBar = {
            Header(
                title = "Jotter",
                onSettingsClick = { /* TODO: Navigate to Settings */ }
            )
        },
        floatingActionButton = {
            FAB(onClick = { /* TODO: Create Note */ })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryBar(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { newCategory ->
                    selectedCategory = newCategory
                },
                onAddCategoryClick = { /* TODO: Open Add Dialog */ }
            )

            // TODO: Add your Note List/Grid here
        }
    }
}