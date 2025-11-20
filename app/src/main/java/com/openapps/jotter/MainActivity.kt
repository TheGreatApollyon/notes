package com.openapps.jotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.openapps.jotter.ui.screens.homescreen.HomeScreen
import com.openapps.jotter.ui.theme.JotterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Expressive apps should always draw behind system bars
        enableEdgeToEdge()
        setContent {
            JotterTheme {
                HomeScreen()
            }
        }
    }
}