package com.example.moviesdemoapp.engine.sdui

import androidx.compose.runtime.mutableStateMapOf
import com.example.moviesdemoapp.core.network.model.FormStatusDetail
import com.google.gson.Gson

object FormDataStorage {
    var formStatus: Map<String, FormStatusDetail>? = null
    var formData: Map<String, String>? = null
    val formDataStoreAndValidation = mutableStateMapOf<String, String>()
    fun clearFormData() = formDataStoreAndValidation.clear()
    fun getCurrentFormData(): Map<String, String> = formDataStoreAndValidation
    fun readAndSetValue(screenName: String?, key: String?): String {
        return formDataStoreAndValidation[key] ?: formData?.get(key)
        ?: formStatus?.get(screenName)?.formData?.get(key) ?: ""
    }

    fun readAndSetValue(key: String?): String = readAndSetValue(null, key)
    fun validateForm(screenName:String?, sduiViewModel:SDUIViewModel): Boolean {
        if(formDataStoreAndValidation.isEmpty()){
            return true
        }
        val  emptyKeys: List<String> = formDataStoreAndValidation.keys.filter { key-> formDataStoreAndValidation[key].isNullOrEmpty()  }.map { key-> formDataStoreAndValidation[key] as String }
        if(!screenName.isNullOrBlank() && emptyKeys.isNotEmpty()){
            val formData = sduiViewModel.checkFormCompleted(screenName)
        }

       // return formDataStoreAndValidation.values.all { it.isNotEmpty() && it.isNotBlank() }
        return true
    }

    fun updateFormData(screenName: String, key: String, value: String) {
        formStatus?.get(screenName)?.let {
            formDataStoreAndValidation[key] = value
        }
    }

    fun getFormJsonData(screenName: String): String {
       return Gson().toJson(formStatus?.getOrDefault(screenName,  ""))
    }

}
