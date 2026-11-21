package com.mahiuddin.magicstory.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mahiuddin.magicstory.data.local.AssetTemplateDataSource
import com.mahiuddin.magicstory.data.repository.TemplateRepositoryImpl
import com.mahiuddin.magicstory.ui.editor.EditorScreen
import com.mahiuddin.magicstory.ui.editor.EditorViewModel
import com.mahiuddin.magicstory.ui.home.HomeScreen
import com.mahiuddin.magicstory.ui.home.HomeViewModel
import com.mahiuddin.magicstory.ui.templates.TemplatesScreen
import com.mahiuddin.magicstory.ui.theme.AccentRed
import com.mahiuddin.magicstory.ui.theme.DeepRed
import com.mahiuddin.magicstory.ui.theme.MainBackground
import com.mahiuddin.magicstory.ui.theme.PrimaryRed
import com.mahiuddin.magicstory.ui.theme.SecondaryText
import com.mahiuddin.magicstory.ui.theme.SoftRedShadow
import com.mahiuddin.magicstory.ui.theme.SubtleBorder
import com.mahiuddin.magicstory.ui.theme.SurfaceVariantLight

object NavRoutes {
    const val HOME = "home"
    const val TEMPLATES = "templates"
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

    val homeViewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(repository) as T
            }
        }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute == NavRoutes.HOME || currentRoute == NavRoutes.TEMPLATES

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(NavRoutes.HOME) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onTemplateSelect = { templateId ->
                        navController.navigate(NavRoutes.editorRoute(templateId))
                    }
                )
            }

            composable(NavRoutes.TEMPLATES) {
                TemplatesScreen(
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
                val templateId = backStackEntry.arguments?.getString("templateId") ?: "minimalist_journal"

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

        if (showBottomBar) {
            MagicBottomNavigationBar(
                currentRoute = currentRoute ?: NavRoutes.HOME,
                onNavigateHome = {
                    if (currentRoute != NavRoutes.HOME) {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateTemplates = {
                    if (currentRoute != NavRoutes.TEMPLATES) {
                        navController.navigate(NavRoutes.TEMPLATES) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onCreateNew = {
                    navController.navigate(NavRoutes.editorRoute("minimalist_journal"))
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun MagicBottomNavigationBar(
    currentRoute: String,
    onNavigateHome: () -> Unit,
    onNavigateTemplates: () -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 10.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Wide Floating White Pill Navigation Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            color = MainBackground,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, SubtleBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Tab: Home
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNavigateHome() }
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = if (currentRoute == NavRoutes.HOME) PrimaryRed else SecondaryText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Home",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentRoute == NavRoutes.HOME) PrimaryRed else SecondaryText
                    )
                }

                Spacer(modifier = Modifier.width(56.dp)) // Space for Floating FAB

                // Right Tab: Templates
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNavigateTemplates() }
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = "Templates",
                        tint = if (currentRoute == NavRoutes.TEMPLATES) PrimaryRed else SecondaryText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Templates",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentRoute == NavRoutes.TEMPLATES) PrimaryRed else SecondaryText
                    )
                }
            }
        }

        // Center Floating Add Button with Light-Pink Outer Ring Stroke & Soft Glow
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Light-Pink Ring Stroke & Soft Glow
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(14.dp, CircleShape, spotColor = SoftRedShadow, ambientColor = SoftRedShadow),
                shape = CircleShape,
                color = SurfaceVariantLight,
                border = BorderStroke(1.5.dp, Color(0xFFFFD1D6))
            ) {}

            // Main Gradient Circle Button
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryRed, AccentRed, DeepRed)
                        )
                    )
                    .clickable { onCreateNew() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Create Story",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
