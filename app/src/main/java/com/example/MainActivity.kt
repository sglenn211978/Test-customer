package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AnalyticsDashboardScreen
import com.example.ui.screens.EarTrainingQuizScreen
import com.example.ui.screens.ExploreFretboardScreen
import com.example.ui.screens.FindNoteQuizScreen
import com.example.ui.screens.NoteIdQuizScreen
import com.example.ui.screens.SongPracticeScreen
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FretboardMasteryTheme
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.FretboardViewModel
import com.example.ui.viewmodel.QuizViewModel
import com.example.ui.viewmodel.SongPracticeViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Explore : Screen("explore", "Neck", Icons.Default.GridOn)
    object Songs : Screen("songs", "Songs", Icons.Default.QueueMusic)
    object NoteId : Screen("note_id", "Identify", Icons.Default.Quiz)
    object EarTrain : Screen("ear_train", "Ear Train", Icons.Default.Hearing)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FretboardMasteryTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val navController = rememberNavController()
    val fretboardViewModel: FretboardViewModel = viewModel()
    val songPracticeViewModel: SongPracticeViewModel = viewModel()
    val quizViewModel: QuizViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()

    val screens = listOf(
        Screen.Explore,
        Screen.Songs,
        Screen.NoteId,
        Screen.EarTrain,
        Screen.Analytics
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Explore.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = (currentRoute == screen.route)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Explore.route) {
                ExploreFretboardScreen(viewModel = fretboardViewModel)
            }
            composable(Screen.Songs.route) {
                SongPracticeScreen(viewModel = songPracticeViewModel)
            }
            composable(Screen.NoteId.route) {
                NoteIdQuizScreen(viewModel = quizViewModel)
            }
            composable(Screen.EarTrain.route) {
                EarTrainingQuizScreen(viewModel = quizViewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsDashboardScreen(viewModel = analyticsViewModel)
            }
        }
    }
}
