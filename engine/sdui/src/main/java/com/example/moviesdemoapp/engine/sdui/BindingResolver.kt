package com.example.moviesdemoapp.engine.sdui

import android.content.Context
import com.example.moviesdemoapp.core.network.model.BindingItem

class BindingResolver (val context: Context) {

    private var bindings: Map<String, BindingItem> = emptyMap()

    fun loadBindings(bindings: Map<String, BindingItem>) {
        this.bindings = bindings
    }

    fun resolve(key:String?): String {
        try {
            val bindingObj = bindings.getOrDefault(key, null)
            if (bindingObj != null) {
                return when (bindingObj.source) {

                    "string" -> context.getString(
                        context.resources.getIdentifier(
                            bindingObj.key,
                            "string",
                            context.packageName
                        )
                    )

                    // future extension
                    "api" -> fetchFromApi(bindingObj.key)

                    "form" -> getFormValue(bindingObj.key)

                    "template" -> fetchTemplateValues(bindingObj.key)

                    else -> key ?: ""
                }
            }
            return key ?: ""
        } catch (e: Exception) {
            // In case of any error during binding resolution, return the key as fallback
            return key ?: ""
        }
    }

    private fun fetchTemplateValues(key: String): String {
        var result = ""
        key.replace("{{", "").replace("}}", "").split(",").map { it.trim() }.forEach {
            result += FormDataStorage.readAndSetValue(it) + ","
        }
        return "API_VALUE_$key"
    }

    private fun fetchFromApi(key: String): String {
        return "API_VALUE_$key"
    }

    private fun getFormValue(key: String): String {
        return "FORM_VALUE_$key"
    }
}