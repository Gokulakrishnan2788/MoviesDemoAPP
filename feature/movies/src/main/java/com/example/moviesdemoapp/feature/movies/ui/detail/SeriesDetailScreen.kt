package com.example.moviesdemoapp.feature.movies.ui.detail

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.moviesdemoapp.core.ui.DesignTokens
import com.example.moviesdemoapp.feature.movies.domain.model.SeriesDetail
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SeriesDetailScreen(
    seriesId: String,
    navController: NavController,
    viewModel: SeriesDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(seriesId) {
        viewModel.handleIntent(SeriesDetailIntent.Load(seriesId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SeriesDetailEffect.GoBack -> navController.popBackStack()
            }
        }
    }

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
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(DesignTokens.SpacingMd),
            )
            state.detail != null -> DetailContent(
                detail = state.detail!!,
                isInWatchlist = state.isInWatchlist,
                onBack = { viewModel.handleIntent(SeriesDetailIntent.NavigateBack) },
                onToggleWatchlist = { viewModel.handleIntent(SeriesDetailIntent.ToggleWatchlist) },
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: SeriesDetail,
    isInWatchlist: Boolean,
    onBack: () -> Unit,
    onToggleWatchlist: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DesignTokens.PrimaryText)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleWatchlist) {
                Icon(
                    if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Watchlist",
                    tint = DesignTokens.Accent,
                )
            }
        }

        // Poster + meta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = detail.posterUrl,
                contentDescription = detail.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(176.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(detail.title, color = DesignTokens.PrimaryText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${detail.year}  •  ${detail.runtime}", color = DesignTokens.SecondaryText, fontSize = 14.sp)
                Text(detail.genre, color = DesignTokens.SecondaryText, fontSize = 13.sp)
                if (detail.totalSeasons.isNotEmpty()) {
                    Text("${detail.totalSeasons} seasons", color = DesignTokens.SecondaryText, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, null, tint = DesignTokens.Accent, modifier = Modifier.size(16.dp))
                    Text("IMDb ${detail.rating}", color = DesignTokens.PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (detail.plot.isNotEmpty()) {
            SectionLabel("Plot")
            Text(detail.plot, color = DesignTokens.PrimaryText, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (detail.actors.isNotEmpty()) {
            SectionLabel("Cast")
            Text(detail.actors, color = DesignTokens.SecondaryText, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (detail.director.isNotEmpty()) {
            SectionLabel("Director")
            Text(detail.director, color = DesignTokens.SecondaryText, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (detail.awards.isNotEmpty()) {
            SectionLabel("Awards")
            Text(detail.awards, color = DesignTokens.SecondaryText, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        color = DesignTokens.PrimaryText,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp),
    )
}
