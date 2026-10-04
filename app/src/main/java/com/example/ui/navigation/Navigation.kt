package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "خانه", Icons.Default.Home)
    object Finder : Screen("finder", "ماهواره‌یاب", Icons.Default.Explore)
    object Signal : Screen("signal", "سیگنال‌متر", Icons.Default.Sensors)
    object Frequency : Screen("frequency", "فرکانس‌ها", Icons.Default.List)
    object Repair : Screen("repair", "تعمیرگاه", Icons.Default.Build)
    object Calculators : Screen("calculators", "محاسبات", Icons.Default.Calculate)
    object Business : Screen("business", "مشتریان", Icons.Default.Assignment)
    object AiAssistant : Screen("ai", "دستیار AI", Icons.Default.AutoAwesome)
    object Settings : Screen("settings", "تنظیمات", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Finder,
    Screen.Signal,
    Screen.Frequency,
    Screen.Business
)
