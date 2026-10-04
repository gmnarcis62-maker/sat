package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PersianRtlProvider
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavItems
import com.example.ui.screens.ai.AiAssistantScreen
import com.example.ui.screens.business.BusinessHubScreen
import com.example.ui.screens.calculator.CalculatorsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.finder.DishAlignmentScreen
import com.example.ui.screens.frequency.FrequencyBankScreen
import com.example.ui.screens.repair.RepairLabScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.signal.SignalMeterScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SignalCyan
import com.example.ui.theme.SpaceNavyDark
import com.example.ui.theme.SpaceNavySurface
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PersianRtlProvider {
                    val viewModel: MainViewModel = viewModel()
                    var currentRoute by remember { mutableStateOf(Screen.Dashboard.route) }

                    // Hardware back button handler: go back to dashboard if on sub-screen
                    BackHandler(enabled = currentRoute != Screen.Dashboard.route) {
                        currentRoute = Screen.Dashboard.route
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = SpaceNavyDark,
                        bottomBar = {
                            NavigationBar(
                                containerColor = SpaceNavySurface,
                                contentColor = SignalCyan,
                                tonalElevation = 8.dp
                            ) {
                                bottomNavItems.forEach { screen ->
                                    val isSelected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentRoute = screen.route },
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SpaceNavyDark,
                                            selectedTextColor = SignalCyan,
                                            indicatorColor = SignalCyan,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentRoute) {
                                Screen.Dashboard.route -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToFinder = { currentRoute = Screen.Finder.route },
                                    onNavigateToSignal = { currentRoute = Screen.Signal.route },
                                    onNavigateToFrequency = { currentRoute = Screen.Frequency.route },
                                    onNavigateToRepair = { currentRoute = Screen.Repair.route },
                                    onNavigateToCalculators = { currentRoute = Screen.Calculators.route },
                                    onNavigateToBusiness = { currentRoute = Screen.Business.route },
                                    onNavigateToAi = { currentRoute = Screen.AiAssistant.route },
                                    onNavigateToSettings = { currentRoute = Screen.Settings.route }
                                )
                                Screen.Finder.route -> DishAlignmentScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                                Screen.Signal.route -> SignalMeterScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                                Screen.Frequency.route -> FrequencyBankScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                                Screen.Repair.route -> RepairLabScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                                Screen.Calculators.route -> CalculatorsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                                Screen.Business.route -> BusinessHubScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                                Screen.AiAssistant.route -> AiAssistantScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                                Screen.Settings.route -> SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentRoute = Screen.Dashboard.route }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
