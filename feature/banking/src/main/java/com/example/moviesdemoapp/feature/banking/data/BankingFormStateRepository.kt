package com.example.moviesdemoapp.feature.banking.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.moviesdemoapp.core.data.ScreenRepository
import com.example.moviesdemoapp.core.network.model.FormStatusDetail
import com.example.moviesdemoapp.engine.navigation.Routes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages persistence of banking form completion state.
 * Tracks which forms have been completed and when.
 */
@Singleton
class BankingFormStateRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val screenRepository: ScreenRepository,
    private val json: Json,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "banking_form_state",
        Context.MODE_PRIVATE
    )

    // Define the list of known form IDs (now matching navigation routes)
    private val formIds = listOf(
        Routes.BANKING,
        Routes.BANKING_ADDRESS,
        Routes.BANKING_FINENCIAL_DETAIL,
        Routes.BANKING_REVIEW_SUBMIT
    )

    /**
     * Mark a form as completed and save its data dynamically using its ID
     */
    fun markFormCompleted(formId: String, formData: String? = null) {
        prefs.edit {
            putBoolean("form_${formId}_completed", true)
            putString("last_completed_form_id", formId)

            // Save form data if provided
            formData?.let {
                putString("form_${formId}_data", it)
            }
        }
    }

    fun checkFormCompleted(formId: String): Boolean {
       return prefs.getBoolean("form_${formId}_completed",false)
    }

    /**
     * Check if a specific form is completed
     */
    fun isFormCompleted(formId: String): Boolean {
        return prefs.getBoolean("form_${formId}_completed", false)
    }

    /**
     * Get the list of all incomplete form IDs
     */
    fun getIncompleteFormIds(): List<String> {

        return formIds.filter { !isFormCompleted(it) }
    }

    /**
     * Get a random incomplete form ID. 
     * If all are completed, returns the first one to allow restart.
     */
    fun getRandomIncompleteForm(): String {
        val incomplete = getIncompleteFormIds()
        return if (incomplete.isEmpty()) formIds.first() else incomplete.random()
    }

    /**
     * Get the first incomplete form (sequential)
     */
    fun getNextIncompleteForm(): String? {
        return getIncompleteFormIds().firstOrNull()
    }

    /**
     * Get completion status for all forms
     */
    fun getFormCompletionStatus(): FormCompletionStatus {
        val incomplete = getIncompleteFormIds()
        return FormCompletionStatus(
            isForm1Completed = isFormCompleted("1"),
            isForm2Completed = isFormCompleted("2"),
            isForm3Completed = isFormCompleted("3"),
            isForm4Completed = isFormCompleted("4"),
            lastCompletedForm = prefs.getString("last_completed_form_id", "0") ?: "0",
            incompleteForms = incomplete
        )
    }

    /**
     * Get the form to resume from (randomly chosen from incomplete ones)
     */
    fun getFormToResume(useRandom: Boolean = true): String {
        return if (useRandom) {
            getRandomIncompleteForm()
        } else {
            getNextIncompleteForm() ?: formIds.first()
        }
    }

    /**
     * Get saved form data
     */
    fun getFormData(formId: String): String? {
        return prefs.getString("form_${formId}_data", null)
    }

    /**
     * Reset all form state
     */
    fun resetAllForms() {
        prefs.edit { clear() }
    }

    /**
     * Clear specific form data
     */
    fun clearFormData(formId: String) {
        prefs.edit { remove("form_${formId}_data") }
    }

    /**
     * Save the entire form status map and order to persistence
     */
    fun saveFormStatusState(statusMap: Map<String, FormStatusDetail>, order: List<String>?) {
        val statusJson = json.encodeToString(statusMap)
        val orderJson = json.encodeToString(order ?: emptyList<String>())
        prefs.edit {
            putString("full_form_status_map", statusJson)
            putString("form_order", orderJson)
        }
    }

    /**
     * Load the form order from persistence
     */
    fun getFormOrder(): List<String> {
        val jsonString = prefs.getString("form_order", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save the entire form status map to persistence
     */
    fun saveFormStatusMap(statusMap: Map<String, FormStatusDetail>) {
        val jsonString = json.encodeToString(statusMap)
        prefs.edit {
            putString("full_form_status_map", jsonString)
        }
    }

    /**
     * Load the entire form status map from persistence
     */
    fun getFormStatusMap(): Map<String, FormStatusDetail>? {
        val jsonString = prefs.getString("full_form_status_map", null) ?: return null
        return try {
            json.decodeFromString<Map<String, FormStatusDetail>>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resets specific forms based on a list
     */
    fun resetForms(formIdsToReset: List<String>) {
        prefs.edit {
            formIdsToReset.forEach { id ->
                remove("form_${id}_completed")
                remove("form_${id}_data")
            }
            // Also update the full map if it exists
            getFormStatusMap()?.toMutableMap()?.let { map ->
                formIdsToReset.forEach { id ->
                    map[id]?.let { detail ->
                        map[id] = detail.copy(status = "notfilled", formData = null)
                    }
                }
                saveFormStatusMap(map)
            }
        }
    }
}

/**
 * Data class representing form completion status
 */
data class FormCompletionStatus(
    val isForm1Completed: Boolean = false,
    val isForm2Completed: Boolean = false,
    val isForm3Completed: Boolean = false,
    val isForm4Completed: Boolean = false,
    val lastCompletedForm: String = "0",
    val incompleteForms: List<String> = emptyList()
) {
    fun allFormsCompleted(): Boolean = incompleteForms.isEmpty()
    fun canProceedToForm3(): Boolean = isForm1Completed && isForm2Completed
}
