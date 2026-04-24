package com.example.moviesdemoapp.feature.banking.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Manages persistence of banking form completion state.
 * Tracks which forms have been completed and when.
 */
@Singleton
class BankingFormStateRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "banking_form_state",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val FORM_1_COMPLETED = "form_1_personal_details_completed"
        private const val FORM_2_COMPLETED = "form_2_address_completed"
        private const val FORM_3_COMPLETED = "form_3_financial_completed"
        private const val FORM_4_COMPLETED = "form_4_review_completed"
        private const val LAST_COMPLETED_FORM = "last_completed_form"
        private const val FORM_1_DATA = "form_1_data"
        private const val FORM_2_DATA = "form_2_data"
        private const val FORM_3_DATA = "form_3_data"
    }

    /**
     * Mark a form as completed and save its data
     */
    fun markFormCompleted(formNumber: Int, formData: String? = null) {
        val key = when (formNumber) {
            1 -> FORM_1_COMPLETED
            2 -> FORM_2_COMPLETED
            3 -> FORM_3_COMPLETED
            4 -> FORM_4_COMPLETED
            else -> return
        }

        prefs.edit().apply {
            putBoolean(key, true)
            putLong(LAST_COMPLETED_FORM, formNumber.toLong())

            // Save form data if provided
            formData?.let {
                when (formNumber) {
                    1 -> putString(FORM_1_DATA, it)
                    2 -> putString(FORM_2_DATA, it)
                    3 -> putString(FORM_3_DATA, it)
                }
            }
            apply()
        }
    }

    /**
     * Check if a specific form is completed
     */
    fun isFormCompleted(formNumber: Int): Boolean {
        val key = when (formNumber) {
            1 -> FORM_1_COMPLETED
            2 -> FORM_2_COMPLETED
            3 -> FORM_3_COMPLETED
            4 -> FORM_4_COMPLETED
            else -> return false
        }
        return prefs.getBoolean(key, false)
    }

    /**
     * Get the last completed form number
     */
    fun getLastCompletedForm(): Int {
        return prefs.getLong(LAST_COMPLETED_FORM, 0L).toInt()
    }

    /**
     * Get completion status for all forms
     */
    fun getFormCompletionStatus(): FormCompletionStatus {
        return FormCompletionStatus(
            isForm1Completed = isFormCompleted(1),
            isForm2Completed = isFormCompleted(2),
            isForm3Completed = isFormCompleted(3),
            isForm4Completed = isFormCompleted(4),
            lastCompletedForm = getLastCompletedForm()
        )
    }

    /**
     * Get the next incomplete form number
     * Returns the first incomplete form, or null if all forms are complete
     */
    fun getNextIncompleteForm(): Int? {
        for (i in 1..4) {
            if (!isFormCompleted(i)) {
                return i
            }
        }
        return null
    }

    /**
     * Get the form to resume from (where user left off)
     * Returns form 3 if forms 1 & 2 are complete, else returns next incomplete form
     */
    fun getFormToResume(): Int {
        return when {
            !isFormCompleted(1) -> 1
            !isFormCompleted(2) -> 2
            !isFormCompleted(3) -> 3
            !isFormCompleted(4) -> 4
            else -> 1 // All complete, restart from form 1
        }
    }

    /**
     * Get saved form data
     */
    fun getFormData(formNumber: Int): String? {
        val key = when (formNumber) {
            1 -> FORM_1_DATA
            2 -> FORM_2_DATA
            3 -> FORM_3_DATA
            else -> return null
        }
        return prefs.getString(key, null)
    }

    /**
     * Reset all form state (start fresh)
     */
    fun resetAllForms() {
        prefs.edit().apply {
            remove(FORM_1_COMPLETED)
            remove(FORM_2_COMPLETED)
            remove(FORM_3_COMPLETED)
            remove(FORM_4_COMPLETED)
            remove(LAST_COMPLETED_FORM)
            remove(FORM_1_DATA)
            remove(FORM_2_DATA)
            remove(FORM_3_DATA)
            apply()
        }
    }

    /**
     * Clear specific form data
     */
    fun clearFormData(formNumber: Int) {
        val key = when (formNumber) {
            1 -> FORM_1_DATA
            2 -> FORM_2_DATA
            3 -> FORM_3_DATA
            else -> return
        }
        prefs.edit { remove(key) }
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
    val lastCompletedForm: Int = 0
) {
    fun allFormsCompleted(): Boolean = isForm1Completed && isForm2Completed && isForm3Completed && isForm4Completed
    fun canProceedToForm3(): Boolean = isForm1Completed && isForm2Completed
}

