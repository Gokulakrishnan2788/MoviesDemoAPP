package com.example.moviesdemoapp.feature.banking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.navigation.Routes
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageEffect
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageIntent
import kotlinx.coroutines.flow.collectLatest

fun NavGraphBuilder.bankingGraph(
    navController: NavController
) {
    navigation(startDestination = "banking_entry", route = "banking_graph") {

        composable("banking_entry") { entry: NavBackStackEntry ->
            val formStatusViewModel: FormStatusViewModel = hiltViewModel(entry)
            val formDetail by formStatusViewModel._formId.collectAsStateWithLifecycle()

            LaunchedEffect(formDetail) {
                if (formDetail == null) {
                    formStatusViewModel.checkAndNavigateToNextForm()
                } else {
                    navController.navigate(formDetail!!) {
                        popUpTo("banking_entry") { inclusive = true }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DesignTokens.Accent)
            }
        }

        composable(route = Routes.BANKING) { entry: NavBackStackEntry ->
            val viewModel: BankingViewModel = hiltViewModel(entry)

            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    if (effect is BankingPageEffect.Navigate) {
                        navController.navigate(effect.route)
                    }
                }
            }

            BankingScreen(navController, viewModel) { page ->
                viewModel.handleIntent(BankingPageIntent.MarkFormCompleted(Routes.BANKING, page))
            }
        }

        composable(route = Routes.BANKING_ADDRESS) { entry: NavBackStackEntry ->
            val viewModel: BankingViewModel = hiltViewModel(entry)

            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    if (effect is BankingPageEffect.Navigate) {
                        viewModel.handleIntent(BankingPageIntent.MarkFormCompleted(Routes.BANKING_ADDRESS, effect.route))
                        navController.navigate(effect.route)
                    }
                }
            }

            BankingIncrementScreen(navController, viewModel, pageDetail = Routes.BANKING_ADDRESS) { _ -> }
        }

        composable(route = Routes.BANKING_FINENCIAL_DETAIL) { entry: NavBackStackEntry ->
            val viewModel: BankingViewModel = hiltViewModel(entry)

            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    if (effect is BankingPageEffect.Navigate) {
                        viewModel.handleIntent(BankingPageIntent.MarkFormCompleted(Routes.BANKING_FINENCIAL_DETAIL, effect.route))
                        navController.navigate(effect.route)
                    }
                }
            }

            BankingIncrementScreen(navController, viewModel, pageDetail = Routes.BANKING_FINENCIAL_DETAIL) { _ -> }
        }

        composable(route = Routes.BANKING_REVIEW_SUBMIT) { entry: NavBackStackEntry ->
            val viewModel: BankingViewModel = hiltViewModel(entry)

            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    if (effect is BankingPageEffect.Navigate) {
                        viewModel.handleIntent(BankingPageIntent.MarkFormCompleted(Routes.BANKING_REVIEW_SUBMIT, effect.route))
                        navController.navigate(effect.route)
                    }
                }
            }

            val loadingStatus = viewModel.loadingStatus.collectAsStateWithLifecycle()
            if(loadingStatus.value){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DesignTokens.ScreenBackground),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DesignTokens.Accent,
                    )
                }
            } else {
                BankingIncrementScreen(navController, viewModel, pageDetail = Routes.BANKING_REVIEW_SUBMIT) { _ -> }
            }


        }
    }
}
