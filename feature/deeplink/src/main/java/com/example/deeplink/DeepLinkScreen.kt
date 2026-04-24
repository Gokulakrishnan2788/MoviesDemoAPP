package com.example.deeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.deeplink.model.DeepLinkPageEffect
import com.example.deeplink.model.DeepLinkPageIntent
import com.example.moviesdemoapp.engine.sdui.SDUIRenderer

import kotlinx.coroutines.flow.collectLatest

@Composable
fun DeepLinkScreen(navController: NavController = rememberNavController(),
                   viewModel: DeepLinkScreenViewModel = hiltViewModel(), page:String, indexPage:String? = null, onNavigate: ((String) -> Unit)? = null) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(DeepLinkPageIntent.LoadOtherMainPage(page))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DeepLinkPageEffect.Navigate -> {
                    onNavigate?.invoke(effect.route) ?: navController.navigate(effect.route)
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
            viewModel.handleIntent(DeepLinkPageIntent.OnAction(actionId, params))
        },
    )
}
