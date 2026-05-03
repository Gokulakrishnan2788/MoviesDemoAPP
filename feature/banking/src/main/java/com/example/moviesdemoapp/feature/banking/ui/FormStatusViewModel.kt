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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
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
    val _formId: StateFlow<String?> = formId.asStateFlow()

    private val _statusList = MutableStateFlow<Map<String, FormStatusDetail>>(emptyMap())
    val statusList: StateFlow<Map<String, FormStatusDetail>> = _statusList.asStateFlow()

    private val _formOrder = MutableStateFlow<List<String>>(emptyList())
    val formOrder: StateFlow<List<String>> = _formOrder.asStateFlow()

    fun checkAndNavigateToNextForm() {
        viewModelScope.launch {
            // 1. Try to load from local persistence first
            var currentStatus = bankingFormStateRepository.getFormStatusMap() ?: emptyMap()
            var currentOrder = bankingFormStateRepository.getFormOrder()

/*            var currentStatus = emptyMap<String, FormStatusDetail>()
            var currentOrder = emptyList<String>()*/
            val screenModel = loadScreen("personal_details")
            val checkFormStatus = screenModel?.checkFormStatus
            if (currentStatus.isEmpty() || currentOrder.isEmpty()) {
                // 2. Fetch from API or ScreenModel if no local state
                val fetchedData = if (checkFormStatus?.type == "api") {
                    try {
                        val response = when (checkFormStatus.requestType.lowercase()) {
                            "post" -> bankingApi.post(checkFormStatus.endPoint, json.parseToJsonElement("{}"))
                            else -> bankingApi.get(checkFormStatus.endPoint)
                        }
                        
                        val responseString = response.toString()
                        val statusType = object : TypeToken<Map<String, FormStatusDetail>>() {}.type
                        val orderType = object : TypeToken<List<String>>() {}.type
                        
                        val rawMap = gson.fromJson<Map<String, Any>>(responseString, object : TypeToken<Map<String, Any>>() {}.type)
                        
                        val status = if (rawMap.containsKey("formStatus")) {
                            gson.fromJson<Map<String, FormStatusDetail>>(gson.toJson(rawMap["formStatus"]), statusType)
                        } else {
                            gson.fromJson<Map<String, FormStatusDetail>>(responseString, statusType)
                        }
                        
                        val order = if (rawMap.containsKey("formOrder")) {
                            gson.fromJson(gson.toJson(rawMap["formOrder"]), orderType)
                        } else {
                            screenModel.formOrder ?: emptyList()
                        }
                        
                        Pair(status, order)
                    } catch (_: Exception) {
                        Pair(screenModel.formStatus ?: emptyMap(), screenModel.formOrder ?: emptyList())
                    }
                } else {
                    Pair(screenModel?.formStatus ?: emptyMap(), screenModel?.formOrder ?: emptyList())
                }
                
                currentStatus = fetchedData.first
                currentOrder = fetchedData.second
                
                if (currentStatus.isNotEmpty()) {
                    bankingFormStateRepository.saveFormStatusState(currentStatus, currentOrder)
                }
            }

            if(currentOrder.isNotEmpty()){
                try {
                    val response = when (checkFormStatus?.requestType?.lowercase()) {
                        "post" -> bankingApi.post(checkFormStatus.endPoint, json.parseToJsonElement("{}"))
                        else -> bankingApi.get(checkFormStatus?.endPoint?:"")
                    }
                    val responseString = response.toString()
                    val orderType = object : TypeToken<List<String>>() {}.type
                    val rawMap = gson.fromJson<Map<String, Any>>(responseString, object : TypeToken<Map<String, Any>>() {}.type)

                    currentOrder = if (rawMap.containsKey("formOrder")) {
                        gson.fromJson(gson.toJson(rawMap["formOrder"]), orderType)
                    } else {
                        screenModel?.formOrder ?: emptyList()
                    }

                } catch (_: Exception) {
                }
            }

            _statusList.value = currentStatus
            _formOrder.value = currentOrder

            // 3. Determine next form to show based on formOrder and status
            for (id in currentOrder) {
                val detail = currentStatus[id]
                val status = detail?.status?.lowercase() ?: "notfilled"
                
                // Redirect to the first one that is NOT "filled" or "completed"
                if (status != "filled" && status != "completed") {
                    formId.value = id
                    return@launch
                }
            }

            // If everything is completed, default to the start or a completion screen
            formId.value = currentOrder.firstOrNull() ?: Routes.BANKING
        }
    }

    /**
     * Updates the form status and data at runtime and persists it.
     */
    fun updateFormStatus(formId: String, status: String, formData: Map<String, String>? = null) {
        viewModelScope.launch {
            val current = _statusList.value.toMutableMap()
            val existing = current[formId] ?: FormStatusDetail(status = status)

            val updatedDetail = existing.copy(
                status = status,
                formData = formData ?: existing.formData
            )

            current[formId] = updatedDetail
            _statusList.value = current

            // 1. Update local persistence
            bankingFormStateRepository.saveFormStatusState(current, _formOrder.value)

            // 2. Update Dummy Server (Interceptor)
            try {
                val fullPayload = mapOf(
                    "formOrder" to _formOrder.value,
                    "formStatus" to current
                )
                // Use Gson to convert the map to a JSON string, then parse with kotlinx.serialization.json
                val jsonString = gson.toJson(fullPayload)
                val jsonElement = json.parseToJsonElement(jsonString)
                
                bankingApi.post("formStatus", jsonElement)
            } catch (e: Exception) {
                // Silently handle or log server update failure
            }

            if (status.equals("completed", ignoreCase = true) || status.equals("filled", ignoreCase = true)) {
                bankingFormStateRepository.markFormCompleted(formId)

                if (!updatedDetail.formNeedTobeDeleteAfterSubmit.isNullOrEmpty()) {
                    bankingFormStateRepository.resetForms(updatedDetail.formNeedTobeDeleteAfterSubmit!!)
                    _statusList.value = bankingFormStateRepository.getFormStatusMap() ?: current
                }
            }
        }
    }

    private suspend fun loadScreen(screenName: String): ScreenModel? {
        return viewModelScope.async {
            screenRepository.loadScreen(screenName)
        }.await()
    }
}
