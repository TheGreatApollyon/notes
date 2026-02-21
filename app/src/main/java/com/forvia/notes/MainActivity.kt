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

package com.forvia.notes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.forvia.notes.navigation.AppNavHost
import com.forvia.notes.navigation.AppRoutes
import com.forvia.notes.data.repository.UserPreferencesRepository
import com.forvia.notes.ui.theme.NotesTheme
import com.forvia.notes.utils.BiometricAuthUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val userPreferences by userPreferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(initialValue = null)
            val lifecycleOwner = LocalLifecycleOwner.current
            
            var isAppAuthenticated by rememberSaveable { mutableStateOf(false) }
            var authTrigger by rememberSaveable { mutableIntStateOf(0) }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            if (!isChangingConfigurations) {
                                isAppAuthenticated = false
                            }
                        }
                        Lifecycle.Event.ON_START -> {
                            authTrigger++
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            LaunchedEffect(userPreferences?.isAppLockEnabled) {
                if (userPreferences?.isAppLockEnabled == false) {
                    isAppAuthenticated = true
                }
            }

            LaunchedEffect(authTrigger, userPreferences == null) {
                if (userPreferences?.isAppLockEnabled == true && !isAppAuthenticated) {
                    BiometricAuthUtil.authenticate(
                        activity = this@MainActivity,
                        title = "Notes Locked",
                        subtitle = "Authenticate To Open The App",
                        onSuccess = { isAppAuthenticated = true },
                        onError = { finish() }
                    )
                }
            }

            NotesTheme(
                isTrueBlackEnabled = userPreferences?.isTrueBlackEnabled ?: false,
                isHapticEnabled = userPreferences?.isHapticEnabled ?: true
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        startDestination = AppRoutes.Home,
                        userPreferencesRepository = userPreferencesRepository
                    )
                }
            }
        }
    }
}
