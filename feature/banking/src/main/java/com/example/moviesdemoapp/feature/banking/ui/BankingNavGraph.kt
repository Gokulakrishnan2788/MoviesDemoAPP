package com.example.moviesdemoapp.feature.banking.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.moviesdemoapp.engine.navigation.Routes

fun NavGraphBuilder.bankingGraph(navController: NavController) {
    var page:String? = null
    navigation(startDestination = Routes.BANKING, route = "banking_graph") {
        composable(Routes.BANKING) {
            BankingScreen(navController){
                page = it
                navController.popBackStack()
            }
        }
        composable(route = Routes.BANKING_ADDRESS) {
            BankingIncrementScreen(navController, pageDetail = Routes.BANKING_ADDRESS){
                navController.popBackStack()
            }
        }
        composable(route = Routes.BANKING_FINENCIAL_DETAIL) {
            BankingIncrementScreen(navController, pageDetail = Routes.BANKING_FINENCIAL_DETAIL){
                navController.popBackStack()
            }
        }
        composable(route = Routes.BANKING_REVIEW_SUBMIT) {
            BankingIncrementScreen(navController, pageDetail = Routes.BANKING_REVIEW_SUBMIT){
                navController.popBackStack()
            }
        }
        if(page != null) {
            composable(page) {
                BankingIncrementScreen(navController, pageDetail = page) {
                    navController.popBackStack()
                }
            }
        }

    }
}
