package com.mahiuddin.magicstory.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mahiuddin.magicstory.data.local.AssetTemplateDataSource
import com.mahiuddin.magicstory.data.repository.TemplateRepositoryImpl
import com.mahiuddin.magicstory.ui.editor.EditorScreen
import com.mahiuddin.magicstory.ui.editor.EditorViewModel
import com.mahiuddin.magicstory.ui.home.HomeScreen
import com.mahiuddin.magicstory.ui.home.HomeViewModel

object NavRoutes {
    const val HOME = "home"
    const val EDITOR = "editor/{templateId}"

    fun editorRoute(templateId: String) = "editor/$templateId"
}

@Composable
fun MagicStoryNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val repository = remember(context) {
        val dataSource = AssetTemplateDataSource(context.applicationContext)
        TemplateRepositoryImpl(dataSource)
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return HomeViewModel(repository) as T
                    }
                }
            )

            HomeScreen(
                viewModel = homeViewModel,
                onTemplateSelect = { templateId ->
                    navController.navigate(NavRoutes.editorRoute(templateId))
                }
            )
        }

        composable(
            route = NavRoutes.EDITOR,
            arguments = listOf(
                navArgument("templateId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: "fashion_drop_001"

            val editorViewModel: EditorViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return EditorViewModel(repository) as T
                    }
                }
            )

            EditorScreen(
                templateId = templateId,
                viewModel = editorViewModel,
                onBackPress = { navController.popBackStack() }
            )
        }
    }
}
