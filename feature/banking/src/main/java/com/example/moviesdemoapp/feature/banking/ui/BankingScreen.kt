package com.example.moviesdemoapp.feature.banking.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.app.Activity
import androidx.compose.foundation.background
import androidx.navigation.NavController
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.engine.sdui.SDUIRenderer
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageEffect
import com.example.moviesdemoapp.feature.banking.ui.model.BankingPageIntent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BankingScreen(
    navController: NavController,
    viewModel: BankingViewModel = hiltViewModel(),
    onFormComplete: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if(loadingState){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = DesignTokens.Accent,
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(BankingPageIntent.LoadPersonalDetailMainPage)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BankingPageEffect.Navigate -> {
                    navController.navigate(effect.route)
                    onFormComplete(effect.route)
                }
                is BankingPageEffect.AutoNavigate -> {
                    // Handle auto-navigation
                }
                is BankingPageEffect.StartActivityAndFinish -> {
                    val intent = Intent(context, effect.activityClass)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
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
        onAction = { currentActionId, actionId, params, node ->
            if ((actionId.equals("navigate", ignoreCase = true) || actionId.equals("navigation", ignoreCase = true)) && params.containsKey("route")) {
                val formStatus = viewModel.getFormStatus()
                val formStatusData = formStatus[currentActionId]
                formStatusData?.status = "completed"
                formStatus[currentActionId] = formStatusData ?: return@SDUIRenderer
                viewModel.saveFormStatus(formStatus)
                onFormComplete(params["route"] ?: "")
            }
            viewModel.handleIntent(BankingPageIntent.OnAction(actionId, params, node))
        },
    )
}

@Composable
fun BankingIncrementScreen(
    navController: NavController,
    viewModel: BankingViewModel = hiltViewModel(),
    pageDetail: String?,
    onFormComplete: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val loadingStatus by viewModel.loadingStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (pageDetail == null) {
            viewModel.handleIntent(BankingPageIntent.LoadPersonalDetailMainPage)
        } else {
            viewModel.handleIntent(BankingPageIntent.LoadOtherMainPage(pageDetail))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BankingPageEffect.Navigate -> {
                    onFormComplete(effect.route)
                    navController.navigate(effect.route)
                }
                is BankingPageEffect.AutoNavigate -> {
                    // Handle auto-navigation to next form
                }
                is BankingPageEffect.StartActivityAndFinish -> {
                    val intent = Intent(context, effect.activityClass)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            }
        }
    }
    if(loadingStatus){
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
        SDUIRenderer(
            screenModel = state.screenModel,
            isLoading = state.isLoading,
            error = state.error,
            dataMap = state.dataMap,
            listData = state.listData,
            onAction = { currentFormId, actionId, params, node ->
                if ((actionId == "navigate" || actionId == "navigation") && params.containsKey("route")) {
                    val formStatus = viewModel.getFormStatus()
                    val formStatusData = formStatus[currentFormId]
                    formStatusData?.status = "completed"
                    formStatus[currentFormId] = formStatusData ?: return@SDUIRenderer
                    viewModel.saveFormStatus(formStatus)
                    onFormComplete(params["route"] ?: "")
                }
                viewModel.handleIntent(BankingPageIntent.OnAction(actionId, params, node))
            },
        )
    }

}

