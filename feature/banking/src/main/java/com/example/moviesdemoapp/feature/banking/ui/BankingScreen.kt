package com.example.moviesdemoapp.feature.banking.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.moviesdemoapp.engine.sdui.SDUIRenderer
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageEffect
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageIntent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BankingScreen(navController: NavController,
                   viewModel: BankingViewModel = hiltViewModel(), page:(String)-> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(BankingPageIntent.LoadPersonalDetailMainPage)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BankingPageEffect.Navigate -> navController.navigate(effect.route)
            }
        }
    }

    SDUIRenderer(
        screenModel = state.screenModel,
        isLoading = state.isLoading,
        error = state.error,
        dataMap = state.dataMap,
        listData = state.listData,
        onAction = { actionId, params ->
            if (actionId.equals("navigate", ignoreCase = true) && params.containsKey("route")) {
                page.invoke(params["route"] ?: "")
            }
            viewModel.handleIntent(BankingPageIntent.OnAction(actionId, params))
        },
    )
}

@Composable
fun BankingIncrementScreen(navController: NavController, viewModel: BankingViewModel = hiltViewModel(), pageDetail:String?, page:(String)-> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if(pageDetail == null) {
            viewModel.handleIntent(BankingPageIntent.LoadPersonalDetailMainPage)
        } else {
            viewModel.handleIntent(BankingPageIntent.LoadOtherMainPage(pageDetail))

        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BankingPageEffect.Navigate -> {
                    page.invoke(effect.route)
                    navController.navigate(effect.route)
                }
            }
        }
    }

    SDUIRenderer(
        screenModel = state.screenModel,
        isLoading = state.isLoading,
        error = state.error,
        dataMap = state.dataMap,
        listData = state.listData,
        onAction = { actionId, params ->
            if(actionId == "navigate" && params.containsKey("route")){
                page.invoke(params["route"] ?: "")
            }
            viewModel.handleIntent(BankingPageIntent.OnAction(actionId, params))
        },
    )
}

