package com.example.moviesdemoapp.engine.sdui.usecase

import com.example.moviesdemoapp.engine.sdui.ScreenModel
import com.example.moviesdemoapp.engine.sdui.SDUIRepository
import javax.inject.Inject

// Use case: load a screen definition from assets by screenId.
// ViewModel calls this; it delegates to SDUIRepository (asset loader).
class LoadSDUIScreenUseCase @Inject constructor(
    private val repository: SDUIRepository,
) {
    operator fun invoke(screenId: String): ScreenModel? = repository.loadScreen(screenId)
}
