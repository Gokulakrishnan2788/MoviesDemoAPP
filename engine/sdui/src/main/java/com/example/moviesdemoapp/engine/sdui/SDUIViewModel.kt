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

    fun checkFormCompleted(formId: String):String? {
       return prefs.getString("form_${formId}_data", null)
    }

    fun saveFieldData(key: String, value: String) {
        prefs.edit {
            putString("field_$key", value)
        }
    }

    fun getFieldData(key: String): String {
        if(key.contains(",")){
            val regex = "\\{\\{(.+?)\\}\\}".toRegex()
            return regex.replace(key) { matchResult ->
                val key = matchResult.groupValues[1]
                prefs.getString("field_$key", "") ?: ""
            }
        }
        if (key.contains("{{") && key.contains("}}") && !key.endsWith("}}")) {
            val regex = "\\{\\{(.+?)\\}\\}".toRegex()
            // .replace replaces the MATCHES, but keeps everything else as is
            return regex.replace(key) { matchResult ->
                val placeholderKey = matchResult.groupValues[1]
                // Look up the value from SharedPreferences
                prefs.getString("field_$placeholderKey", "") ?: ""
            }
        }
        else {
            var initialChar = ""
            if(key.startsWith("$")){
                initialChar = key.first().toString()
            }
            return initialChar + prefs.getString("field_${key.replace("$", "").replace("{{","").replace("}}", "")}", "")?.replace("$$","$") ?: ""
        }

    }
}