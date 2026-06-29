package com.example.emmo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.emmo.ui.screens.*
import com.example.emmo.viewmodel.SleepDetailViewModel
import com.example.emmo.viewmodel.SleepListViewModel

object Routes {
    const val ALARM = "alarm"
    const val ANALYSIS = "analysis"
    const val STATS = "stats"
    const val PROFILE = "profile"
    const val SLEEP_DETAIL = "sleep_detail/{recordId}"
    const val SLEEP_EDIT = "sleep_edit/{recordId}"

    fun sleepDetail(recordId: Long) = "sleep_detail/$recordId"
    fun sleepEdit(recordId: Long) = "sleep_edit/$recordId"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    sleepListViewModel: SleepListViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application

    NavHost(
        navController = navController,
        startDestination = Routes.ALARM,
        modifier = modifier
    ) {
        // 闹钟
        composable(Routes.ALARM) {
            AlarmScreen()
        }

        // 分析
        composable(Routes.ANALYSIS) {
            AnalysisScreen(sleepListViewModel = sleepListViewModel)
        }

        // 统计
        composable(Routes.STATS) {
            HomeScreen(
                viewModel = sleepListViewModel,
                onRecordClick = { recordId -> navController.navigate(Routes.sleepDetail(recordId)) },
                onAddRecordClick = { navController.navigate(Routes.sleepEdit(-1L)) },
                onSettingsClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        // 我的
        composable(Routes.PROFILE) {
            ProfileScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }

        // 详情
        composable(
            route = Routes.SLEEP_DETAIL,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: return@composable
            val detailViewModel: SleepDetailViewModel = viewModel(
                factory = SleepDetailViewModelFactory(application)
            )
            SleepDetailScreen(
                recordId = recordId,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Routes.sleepEdit(id)) }
            )
        }

        // 编辑
        composable(
            route = Routes.SLEEP_EDIT,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: return@composable
            val detailViewModel: SleepDetailViewModel = viewModel(
                factory = SleepDetailViewModelFactory(application)
            )
            SleepEditScreen(
                recordId = if (recordId == -1L) null else recordId,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}
