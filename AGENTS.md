# AGENTS.md

## Goal

Building "Notes" (formerly FNotes), an Android note-taking app with Material 3 Expressive UI. The app has been refactored from `com.openappslabs.jotter` to `com.forvia.notes`.

## Instructions

- App follows system theme (dark/light) with Material You (Monet) dynamic colors by default
- True Black Mode setting only visible when system is in dark theme
- Use device's default 12/24 hour time format (no manual setting)
- Navigation should have 400ms throttle to prevent multiple rapid clicks
- Notes auto-save on back navigation from note detail screen
- The 3-dot menu in note detail screen should use DropdownMenu with Pin/Archive/Trash options

## Discoveries

- `animateContentSize` on NoteCard needs the modifier to NOT be wrapped in `remember` block for animations to work properly when switching grid/list views
- For dropdown menus that show dynamic state (like Pin/Unpin), capture the state when opening the menu to prevent visual changes before menu closes
- `Modifier.animateItem()` in LazyVerticalStaggeredGrid provides item placement animations
- Navigation throttle pattern uses `mutableLongStateOf` and time comparison to prevent duplicate navigations
- DropdownMenu can be anchored inside a Box wrapper around each item in LazyVerticalStaggeredGrid - it automatically positions relative to the anchor box

## Relevant files / directories

- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/ui/screens/homescreen/HomeScreen.kt` - Home screen with note grid/list, long-press context menu
- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/ui/components/NoteCard.kt` - Note card component with long-press handling
- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/ui/screens/notedetailscreen/NoteDetailScreen.kt` - Note detail screen with DropdownMenu implementation
- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/ui/components/ViewToggleRow.kt` - Grid/List toggle button component
- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/ui/components/NoteActionToggleRow.kt` - 3-button toggle row (Pin/Lock/Archive) for note detail
- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/navigation/AppNavHost.kt` - Navigation with throttle implementation
- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/ui/screens/homescreen/HomeScreenViewModel.kt` - Has `togglePin()`, `archiveNote()`, `deleteNote()` functions
- `/Users/sam/Documents/Apps/FNotes/app/src/main/java/com/forvia/notes/data/repository/UserPreferencesRepository.kt` - User preferences
