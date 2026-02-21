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

package com.forvia.notes.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.forvia.notes.data.repository.UserPreferencesRepository
import com.forvia.notes.ui.screens.aboutscreen.AboutScreen
import com.forvia.notes.ui.screens.addcategoryscreen.AddCategoryScreen
import com.forvia.notes.ui.screens.archivescreen.ArchiveScreen
import com.forvia.notes.ui.screens.backuprestore.BackupRestoreScreen
import com.forvia.notes.ui.screens.homescreen.HomeScreen
import com.forvia.notes.ui.screens.notedetailscreen.NoteDetailScreen
import com.forvia.notes.ui.screens.privacypolicyscreen.PrivacyPolicyScreen
import com.forvia.notes.ui.screens.settingsscreen.SettingsScreen
import com.forvia.notes.ui.screens.trashscreen.TrashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: AppRoutes,
    userPreferencesRepository: UserPreferencesRepository
) {
    val slideAnimationSpec = tween<IntOffset>(durationMillis = 300)
    var lastNavigationTime by remember { mutableLongStateOf(0L) }
    val navigationThrottleMs = 400L

    fun navigateWithThrottle(route: Any) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigationTime >= navigationThrottleMs) {
            lastNavigationTime = currentTime
            navController.navigate(route)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<AppRoutes.Home> {
            HomeScreen(
                onNoteClick = { noteId ->
                    navigateWithThrottle(AppRoutes.NoteDetail(noteId = noteId))
                },
                onAddNoteClick = { category ->
                    navigateWithThrottle(AppRoutes.NoteDetail(category = category))
                },
                onSettingsClick = { navigateWithThrottle(AppRoutes.Settings) }
            )
        }

        composable<AppRoutes.AddCategory>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = slideAnimationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            AddCategoryScreen(onBackClick = navController::popBackStack)
        }

        composable<AppRoutes.Settings>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = slideAnimationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            SettingsScreen(
                onBackClick = navController::popBackStack,
                onArchiveClick = { navigateWithThrottle(AppRoutes.Archive) },
                onTrashClick = { navigateWithThrottle(AppRoutes.Trash) },
                onBackupRestoreClick = { navigateWithThrottle(AppRoutes.BackupRestore) },
                onPrivacyPolicyClick = { navigateWithThrottle(AppRoutes.PrivacyPolicy) },
                onAboutClick = { navigateWithThrottle(AppRoutes.About) }
            )
        }

        composable<AppRoutes.Archive>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = slideAnimationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            ArchiveScreen(
                onBackClick = navController::popBackStack,
                onNoteClick = { noteId ->
                    navigateWithThrottle(AppRoutes.NoteDetail(noteId = noteId))
                }
            )
        }

        composable<AppRoutes.Trash>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = slideAnimationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            TrashScreen(
                onBackClick = navController::popBackStack,
                onNoteClick = { noteId ->
                    navigateWithThrottle(AppRoutes.NoteDetail(noteId = noteId))
                }
            )
        }

        composable<AppRoutes.BackupRestore>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = slideAnimationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            BackupRestoreScreen(onBackClick = navController::popBackStack)
        }

        composable<AppRoutes.PrivacyPolicy>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = slideAnimationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            PrivacyPolicyScreen(onBackClick = navController::popBackStack)
        }

        composable<AppRoutes.About>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = slideAnimationSpec
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            AboutScreen(onBackClick = navController::popBackStack)
        }

        composable<AppRoutes.NoteDetail>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween<IntOffset>(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween<IntOffset>(durationMillis = 300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = slideAnimationSpec
                )
            }
        ) {
            NoteDetailScreen(
                onBackClick = navController::popBackStack,
                onManageCategoryClick = {
                    navigateWithThrottle(AppRoutes.AddCategory)
                },
                onNavigateToArchive = { navigateWithThrottle(AppRoutes.Archive) },
                onNavigateToTrash = { navigateWithThrottle(AppRoutes.Trash) },
                onNavigateToHome = { navigateWithThrottle(AppRoutes.Home) }
            )
        }
    }
}
