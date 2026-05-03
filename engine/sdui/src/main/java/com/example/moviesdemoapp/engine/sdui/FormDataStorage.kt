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
        if(key?.contains(",") == true){
            var value = ""
            key.replace("{{", "").replace("}}", "").split(",").forEach { subKey ->
                value += formDataStoreAndValidation[key] ?: formData?.get(key)
                        ?: formStatus?.get(screenName)?.formData?.get(key) ?: ""
            }
            return value
        } else {
            return formDataStoreAndValidation[key] ?: formData?.get(key)
            ?: formStatus?.get(screenName)?.formData?.get(key) ?: ""
        }
    }

    fun readAndSetValue(key: String?): String = readAndSetValue(null, key)
    fun validateForm(screenName:String?, sduiViewModel:SDUIViewModel): Boolean {
        if(formDataStoreAndValidation.isEmpty()){
            return true
        }
        val  emptyKeys: MutableList<String> = getKeysWithEmptyOrNullValues(formDataStoreAndValidation)
        if(!screenName.isNullOrBlank() && emptyKeys.isNotEmpty()){
            emptyKeys.forEach { key->
                val formValue = sduiViewModel.getFieldData(key)
                formDataStoreAndValidation[key] = formValue

                val inMemoryValue = readAndSetValue(screenName, key)
                if (inMemoryValue.isNotEmpty()){
                    formDataStoreAndValidation[key] = inMemoryValue
                }

                formDataStoreAndValidation[key] =  key.let { sduiViewModel.getFieldData(it) }
                    .replace("$$","$") ?: ""
            }
        }
        if (screenName.equals("personal_details", ignoreCase = true) && emptyKeys.isNotEmpty()) {
            return true
        }
        //return formDataStoreAndValidation.values.all { it.isNotEmpty() && it.isNotBlank() }
        return true
    }


    /**
     * Returns a list of keys whose values are null or empty strings.
     */
    private fun getKeysWithEmptyOrNullValues(map: MutableMap<String, String>): MutableList<String> {
        val result: MutableList<String> = ArrayList()
        for (entry in map.entries) {
            val value = entry.value
            if (value.trim { it <= ' ' }.isEmpty()) {
                result.add(entry.key)
            }
        }
        return result
    }

    fun updateFormData(screenName: String, key: String, value: String) {
        formDataStoreAndValidation[key] = value
    }

    fun getFormJsonData(screenName: String): String {
       return Gson().toJson(formStatus?.getOrDefault(screenName,  ""))
    }

}
