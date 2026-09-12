package com.vitacare.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Rounded.Home),
    Tab("news", "News", Icons.Rounded.Article),
    Tab("diseases", "Diseases", Icons.Rounded.MedicalServices),
    Tab("tips", "Tips", Icons.Rounded.NotificationsActive)
)

@Composable
fun VitaCareRoot() {
    val nav = rememberNavController()
    Scaffold(bottomBar = {
        NavigationBar(containerColor = Color.White) {
            val current by nav.currentBackStackEntryAsState()
            val route = current?.destination?.route
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = route == tab.route,
                    onClick = {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(tab.icon, tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }
    }) { pad ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(pad)) {
            composable("home") { HomeScreen(onOpenDisease = { nav.navigate("disease/$it") }, onSeeAllNews = { nav.navigate("news") }) }
            composable("news") { NewsScreen() }
            composable("diseases") { DiseaseListScreen(onOpen = { nav.navigate("disease/$it") }) }
            composable("disease/{id}") { e ->
                DiseaseDetailScreen(e.arguments?.getString("id"), onBack = { nav.popBackStack() })
            }
            composable("tips") { TipsScreen() }
        }
    }
}
