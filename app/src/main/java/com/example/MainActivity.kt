package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.MathSparkRepository
import com.example.model.ChildProfile
import com.example.model.GameMode
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.screens.BrainLabScreen
import com.example.ui.screens.GameArenaScreen
import com.example.ui.screens.HomePlayScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TrophiesScreen
import com.example.ui.theme.MathSparkTheme
import com.example.viewmodel.MathSparkViewModel
import com.example.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object MathSparkRoutes {
    const val PLAY_TAB = "play_tab"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"
    const val BRAIN_LAB = "brain_lab"
    const val TROPHIES = "trophies"
    const val PLAY_ARENA = "play/{mode}"

    fun playRoute(mode: GameMode): String = "play/${mode.name}"
}

private data class MainNavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val mainLayoutTabs = listOf(
    MainNavTab(
        route = MathSparkRoutes.PLAY_TAB,
        labelRes = R.string.nav_play,
        selectedIcon = Icons.Rounded.SportsEsports,
        unselectedIcon = Icons.Outlined.SportsEsports,
        testTag = "nav_tab_play"
    ),
    MainNavTab(
        route = MathSparkRoutes.LEADERBOARD,
        labelRes = R.string.nav_leaderboard,
        selectedIcon = Icons.Rounded.Leaderboard,
        unselectedIcon = Icons.Outlined.Leaderboard,
        testTag = "nav_tab_leaderboard"
    ),
    MainNavTab(
        route = MathSparkRoutes.PROFILE,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_tab_profile"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MathSparkTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            val viewModel: MathSparkViewModel = viewModel(
                key = user.uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        MathSparkViewModel(
                            repository = MathSparkRepository(db),
                            currentUserId = user.uid,
                            defaultDisplayName = user.displayName ?: "Math Explorer"
                        )
                    }
                }
            )
            MainLayoutScreen(viewModel = viewModel, currentUserId = user.uid)
        }
    }
}

@Composable
fun MainLayoutScreen(
    viewModel: MathSparkViewModel,
    currentUserId: String
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showNavigationBar = currentRoute in mainLayoutTabs.map { it.route }

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val sessionsState by viewModel.sessionsState.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val saveMessage by viewModel.profileSaveMessage.collectAsStateWithLifecycle()

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showNavigationBar && !useNavRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("main_navigation_bar")
                    ) {
                        mainLayoutTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showNavigationBar && useNavRail) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("main_navigation_rail")
                    ) {
                        mainLayoutTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                val activeProfile = when (val state = profileState) {
                    is UiState.Success -> state.data
                    is UiState.Loading -> null
                    is UiState.Error -> ChildProfile(userId = currentUserId)
                }

                if (profileState is UiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val safeProfile = activeProfile ?: ChildProfile(userId = currentUserId)
                    NavHost(
                        navController = navController,
                        startDestination = MathSparkRoutes.PLAY_TAB,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(MathSparkRoutes.PLAY_TAB) {
                            HomePlayScreen(
                                profile = safeProfile,
                                onStartGame = { mode, tier ->
                                    viewModel.startGame(mode, tier)
                                    navController.navigate(MathSparkRoutes.playRoute(mode))
                                },
                                onSelectDifficulty = { tier ->
                                    viewModel.updateDifficultyQuick(tier)
                                },
                                onOpenBrainLab = {
                                    navController.navigate(MathSparkRoutes.BRAIN_LAB)
                                },
                                onOpenTrophies = {
                                    navController.navigate(MathSparkRoutes.TROPHIES)
                                }
                            )
                        }
                        composable(MathSparkRoutes.LEADERBOARD) {
                            LeaderboardScreen(
                                currentUserId = currentUserId,
                                profile = safeProfile,
                                leaderboardState = leaderboardState
                            )
                        }
                        composable(MathSparkRoutes.PROFILE) {
                            ProfileScreen(
                                profile = safeProfile,
                                saveMessage = saveMessage,
                                onSaveProfile = { name, avatar, tier ->
                                    viewModel.saveProfileSettings(name, avatar, tier)
                                },
                                onDismissMessage = { viewModel.clearProfileMessage() }
                            )
                        }
                        composable(MathSparkRoutes.BRAIN_LAB) {
                            BrainLabScreen(
                                profile = safeProfile,
                                sessionsState = sessionsState,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(MathSparkRoutes.TROPHIES) {
                            TrophiesScreen(
                                profile = safeProfile,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = MathSparkRoutes.PLAY_ARENA,
                            arguments = listOf(navArgument("mode") { type = NavType.StringType })
                        ) {
                            GameArenaScreen(
                                gameState = gameState,
                                onSubmitAnswer = { viewModel.submitAnswer(it) },
                                onToggleVisualBlocks = { viewModel.toggleVisualBlocks() },
                                onReplayMemorySteps = { viewModel.replayMemorySteps() },
                                onPlayAgain = {
                                    viewModel.startGame(gameState.mode, gameState.tier)
                                },
                                onExit = {
                                    viewModel.quitGameEarly()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
