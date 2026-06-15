package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Dashboard : Screen("dashboard", "الرئيسية", { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") })
    object Attendance : Screen("attendance", "الحضور", { Icon(Icons.Default.EventAvailable, contentDescription = "Attendance") })
    object Employees : Screen("employees", "الموظفين", { Icon(Icons.Default.People, contentDescription = "Employees") })
    object Shifts : Screen("shifts", "الشفتات", { Icon(Icons.Default.Schedule, contentDescription = "Shifts") })
    object Reports : Screen("reports", "التقارير", { Icon(Icons.Default.Assessment, contentDescription = "Reports") })
    object Settings : Screen("settings", "الإعدادات", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
}

@Composable
fun BottomNav(navController: NavController) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Attendance,
        Screen.Employees,
        Screen.Shifts,
        Screen.Reports,
        Screen.Settings
    )

    NavigationBar(
        containerColor = SurfaceVariant,
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = screen.icon,
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OnPrimaryContainer,
                    unselectedIconColor = OnSurfaceVariant,
                    selectedTextColor = OnPrimaryContainer,
                    unselectedTextColor = OnSurfaceVariant,
                    indicatorColor = PrimaryContainer
                ),
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
