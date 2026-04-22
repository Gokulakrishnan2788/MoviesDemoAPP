package com.example.moviesdemoapp.engine.sdui

import androidx.compose.runtime.mutableStateMapOf

object FormDataStorage {
    val formDataStoreAndValidation = mutableStateMapOf<String, String>()
    fun clearFormData() = formDataStoreAndValidation.clear()
    fun getFormData(): Map<String, String> = formDataStoreAndValidation
    fun readAndSetValue(key: String?): String {
        return formDataStoreAndValidation[key] ?: ""
    }
    fun validateForm(): Boolean {
        if(formDataStoreAndValidation.isEmpty()){
            return true
        }
        return formDataStoreAndValidation.values.all { it.isNotEmpty() && it.isNotBlank() }
    }
}