package com.example.moviesdemoapp.feature.banking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesdemoapp.core.data.ScreenRepository
import com.example.moviesdemoapp.core.network.BankingApi
import com.example.moviesdemoapp.core.network.model.FormStatusDetail
import com.example.moviesdemoapp.core.network.model.ScreenModel
import com.example.moviesdemoapp.engine.navigation.Routes
import com.example.moviesdemoapp.feature.banking.data.BankingFormStateRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class FormStatusViewModel @Inject constructor(
    private val bankingFormStateRepository: BankingFormStateRepository,
    private val screenRepository: ScreenRepository,
    private val bankingApi: BankingApi,
    private val json: Json,
    private val gson: Gson,
) : ViewModel() {

    private val formId: MutableStateFlow<String?> = MutableStateFlow(null)
    val _formId: StateFlow<String?> = formId

    private val validRoutes = setOf(
        Routes.BANKING,
        Routes.BANKING_ADDRESS,
        Routes.BANKING_FINENCIAL_DETAIL,
        Routes.BANKING_REVIEW_SUBMIT
    )

    fun checkAndNavigateToNextForm() {
        viewModelScope.launch {
            // Check persistence first
            val incompleteFromPrefs = bankingFormStateRepository.getIncompleteFormIds()
                .filter { it in validRoutes }
            if (incompleteFromPrefs.isNotEmpty()) {
                formId.value = incompleteFromPrefs.first()
                return@launch
            }

            var statusList: Map<String, FormStatusDetail> = emptyMap()
            val screenModel = loadScreen("personal_details")
            
            val checkFormStatus = screenModel?.checkFormStatus
            if (checkFormStatus?.type == "api") {
                statusList = try {
                    val response = when (checkFormStatus.requestType.lowercase()) {
                        "post" -> bankingApi.post(checkFormStatus.endPoint, json.parseToJsonElement("{}"))
                        else -> bankingApi.get(checkFormStatus.endPoint)
                    }
                    
                    // Handle both wrapped and unwrapped response
                    val rawMap = gson.fromJson<Map<String, Any>>(response.toString(), object : TypeToken<Map<String, Any>>() {}.type)
                    if (rawMap.containsKey("formStatus")) {
                        val nestedType = object : TypeToken<Map<String, FormStatusDetail>>() {}.type
                        gson.fromJson(gson.toJson(rawMap["formStatus"]), nestedType)
                    } else {
                        val type = object : TypeToken<Map<String, FormStatusDetail>>() {}.type
                        gson.fromJson(response.toString(), type)
                    }
                } catch (_: Exception) {
                    screenModel.formStatus ?: emptyMap()
                }
            } else {
                statusList = screenModel?.formStatus ?: emptyMap()
            }
            
            // Filter only valid routes to avoid navigation crashes
            statusList = statusList.filterKeys { it in validRoutes }
            
            screenModel?.formStatus = statusList

            for (entry in statusList.entries) {
                val id = entry.key
                val detail = entry.value
                val isLocalComplete = bankingFormStateRepository.isFormCompleted(id)

                if (!isLocalComplete && !detail.status.equals("completed", ignoreCase = true)) {
                    formId.value = id
                    return@launch
                }
            }

            formId.value = Routes.BANKING
        }
    }

    private suspend fun loadScreen(screenName: String): ScreenModel? {
        return viewModelScope.async {
            screenRepository.loadScreen(screenName)
        }.await()
    }
}
