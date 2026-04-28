package com.example.moviesdemoapp.engine.sdui

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel


class SDUIViewModel(context: Context): ViewModel() {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "banking_form_state",
        android.content.Context.MODE_PRIVATE
    )

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
}