package com.example.moviesdemoapp.feature.banking.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.moviesdemoapp.engine.navigation.Routes
import com.example.moviesdemoapp.feature.banking.data.BankingFormStateRepository
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageEffect
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageIntent
import kotlinx.coroutines.flow.collectLatest

fun NavGraphBuilder.bankingGraph(
    navController: NavController,
    formStateRepository: BankingFormStateRepository
) {
    var selectedPage: String? = null
    var lastNavigatedPage: String? = null

    navigation(startDestination = Routes.BANKING, route = "banking_graph") {
        
        composable(Routes.BANKING) {
            val viewModel: BankingViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            /**
             * On initial entry: Check if user has saved progress
             * If forms 1 & 2 are done, auto-navigate to form 3
             */
            LaunchedEffect(Unit) {
                val completionStatus = formStateRepository.getFormCompletionStatus()
                
                // Auto-navigate to form 3 if forms 1 & 2 are complete
                if (completionStatus.canProceedToForm3() && lastNavigatedPage != Routes.BANKING_FINENCIAL_DETAIL) {
                    navController.navigate(Routes.BANKING_FINENCIAL_DETAIL) {
                        popUpTo(Routes.BANKING) { inclusive = true }
                    }
                    lastNavigatedPage = Routes.BANKING_FINENCIAL_DETAIL
                } else {
                    viewModel.handleIntent(BankingPageIntent.LoadPersonalDetailMainPage)
                }
            }

            // Handle navigation effects
            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is BankingPageEffect.Navigate -> {
                            selectedPage = effect.route
                            navController.popBackStack()
                        }
                        is BankingPageEffect.AutoNavigate -> {
                            // Auto-navigation handled by the logic above
                        }
                    }
                }
            }

            BankingScreen(navController, viewModel) { page ->
                selectedPage = page
                // Mark form 1 as completed
                viewModel.handleIntent(
                    BankingPageIntent.MarkFormCompleted(1, page)
                )
            }
        }

        composable(route = Routes.BANKING_ADDRESS) {
            val viewModel: BankingViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.handleIntent(
                    BankingPageIntent.LoadOtherMainPage(Routes.BANKING_ADDRESS)
                )
            }

            // Mark form 2 as completed when user submits
            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is BankingPageEffect.Navigate -> {
                            selectedPage = effect.route
                            // Mark form 2 as completed
                            viewModel.handleIntent(
                                BankingPageIntent.MarkFormCompleted(2, effect.route)
                            )
                            navController.popBackStack()
                        }
                        is BankingPageEffect.AutoNavigate -> {
                            // Handle auto-navigation to next form
                        }
                    }
                }
            }

            BankingIncrementScreen(navController, viewModel, pageDetail = Routes.BANKING_ADDRESS) { page ->
                selectedPage = page
            }
        }

        composable(route = Routes.BANKING_FINENCIAL_DETAIL) {
            val viewModel: BankingViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.handleIntent(
                    BankingPageIntent.LoadOtherMainPage(Routes.BANKING_FINENCIAL_DETAIL)
                )
            }

            // Mark form 3 as completed when user submits
            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is BankingPageEffect.Navigate -> {
                            selectedPage = effect.route
                            // Mark form 3 as completed
                            viewModel.handleIntent(
                                BankingPageIntent.MarkFormCompleted(3, effect.route)
                            )
                            navController.popBackStack()
                        }
                        is BankingPageEffect.AutoNavigate -> {
                            // Handle auto-navigation
                        }
                    }
                }
            }

            BankingIncrementScreen(
                navController, 
                viewModel, 
                pageDetail = Routes.BANKING_FINENCIAL_DETAIL
            ) { page ->
                selectedPage = page
            }
        }

        composable(route = Routes.BANKING_REVIEW_SUBMIT) {
            val viewModel: BankingViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.handleIntent(
                    BankingPageIntent.LoadOtherMainPage(Routes.BANKING_REVIEW_SUBMIT)
                )
            }

            // Mark form 4 as completed when user submits
            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is BankingPageEffect.Navigate -> {
                            selectedPage = effect.route
                            // Mark form 4 as completed
                            viewModel.handleIntent(
                                BankingPageIntent.MarkFormCompleted(4, effect.route)
                            )
                            navController.popBackStack()
                        }
                        is BankingPageEffect.AutoNavigate -> {
                            // Handle auto-navigation
                        }
                    }
                }
            }

            BankingIncrementScreen(
                navController, 
                viewModel, 
                pageDetail = Routes.BANKING_REVIEW_SUBMIT
            ) { page ->
                selectedPage = page
            }
        }

        // Dynamic route for any additional selected pages
        if (selectedPage != null) {
            composable(selectedPage!!) {
                val viewModel: BankingViewModel = hiltViewModel()
                
                LaunchedEffect(Unit) {
                    viewModel.handleIntent(
                        BankingPageIntent.LoadOtherMainPage(selectedPage!!)
                    )
                }

                BankingIncrementScreen(navController, viewModel, pageDetail = selectedPage) { page ->
                    selectedPage = page
                }
            }
        }
    }
}
