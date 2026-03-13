package com.example.diabetica.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.diabetica.data.repository.JugglucoRepository
import com.example.diabetica.ui.detail.DetailScreen
import com.example.diabetica.ui.main.MainScreen
import com.example.diabetica.ui.settings.SettingsScreen
import com.example.diabetica.ui.splash.SplashScreen
import com.example.diabetica.viewmodel.MainViewModel
import com.example.diabetica.ui.juggluco.JugglucoScreen
@Composable
fun AppNavigation(viewModel: MainViewModel, jugglucoRepository: JugglucoRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    navController.popBackStack()
                    navController.navigate("main")
                }
            )
        }

        composable("main") {
            MainScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable("juggluco") {
            JugglucoScreen(
                repository = jugglucoRepository,
                navController = navController
            )
        }

        composable(
            route = "detail/{recordId}",
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId") ?: 0
            DetailScreen(
                viewModel = viewModel,
                navController = navController,
                recordId = recordId
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}