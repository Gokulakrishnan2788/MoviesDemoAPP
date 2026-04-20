package com.example.moviesdemoapp.feature.banking.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.feature.banking.domain.model.BankingHome
import com.example.moviesdemoapp.feature.banking.domain.model.Transaction

@Composable
fun BankingScreen(
    navController: NavController,
    viewModel: BankingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.ScreenBackground),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = DesignTokens.Accent,
            )
            state.error != null -> Text(
                text = state.error!!,
                color = DesignTokens.SecondaryText,
                modifier = Modifier.align(Alignment.Center).padding(DesignTokens.SpacingMd),
            )
            state.home != null -> BankingContent(home = state.home!!)
        }
    }
}

@Composable
private fun BankingContent(home: BankingHome) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header
        Text("Banking", color = DesignTokens.PrimaryText, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // Balance card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DesignTokens.CardBackground, RoundedCornerShape(22.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Total Balance", color = DesignTokens.SecondaryText, fontSize = 14.sp)
            Text(
                text = "${home.currency} ${home.balance}",
                color = DesignTokens.PrimaryText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Transactions section
        Text("Recent Transactions", color = DesignTokens.PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            home.transactions.forEach { TransactionRow(it) }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    val isCredit = transaction.amount.startsWith("+")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DesignTokens.CardBackground, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(transaction.label, color = DesignTokens.PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(transaction.date, color = DesignTokens.SecondaryText, fontSize = 12.sp)
        }
        Text(
            text = transaction.amount,
            color = if (isCredit) androidx.compose.ui.graphics.Color(0xFF4CAF50) else DesignTokens.Accent,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
