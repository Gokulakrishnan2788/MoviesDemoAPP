package com.example.moviesdemoapp.feature.banking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesdemoapp.core.data.ScreenRepository
import com.example.moviesdemoapp.core.network.model.FormStatusDetail
import com.example.moviesdemoapp.core.network.model.ScreenModel
import com.example.moviesdemoapp.engine.navigation.Routes
import com.example.moviesdemoapp.feature.banking.data.BankingFormStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormStatusViewModel @Inject constructor(
    private val bankingFormStateRepository: BankingFormStateRepository,
    private val screenRepository: ScreenRepository,
) : ViewModel() {

    private val formId: MutableStateFlow<String?> = MutableStateFlow(null)
    val _formId: StateFlow<String?> = formId

    fun checkAndNavigateToNextForm() {
        viewModelScope.launch {
            // Check persistence first
            val incompleteFromPrefs = bankingFormStateRepository.getIncompleteFormIds()
            if (incompleteFromPrefs.isNotEmpty()) {
                // We have a list of incomplete forms from our local tracking
                formId.value = incompleteFromPrefs.first()
                return@launch
            }

            // Fallback to server-driven status if local is all complete or empty
            val screenModel = loadScreen("personal_details")
            val statusList = screenModel?.formStatus ?: emptyMap<String, Map<String, FormStatusDetail>>()

            for (entry in statusList.entries) {
                val id = entry.key
                val detail = entry.value as FormStatusDetail
                val isLocalComplete = bankingFormStateRepository.isFormCompleted(id)

                if (!isLocalComplete && !detail.status.equals("completed", ignoreCase = true)) {
                    // Found the first incomplete form
                    formId.value = id
                    return@launch
                }
            }
            
            // If everything is complete, default to the first page
            formId.value = Routes.BANKING
        }
    }

    private suspend fun loadScreen(screenName: String): ScreenModel? {
        return viewModelScope.async {
            screenRepository.loadScreen(screenName)
        }.await()
    }
}
