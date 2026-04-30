package com.example.moviesdemoapp.core.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import androidx.core.content.edit

/**
 * A mock interceptor that simulates a server for banking form status.
 * It uses SharedPreferences to persist state between app sessions.
 */
class MockBankingInterceptor(context: Context) : Interceptor {
    private val prefs = context.getSharedPreferences("mock_server_db", Context.MODE_PRIVATE)
    
    // Initial dummy data as requested
    private val defaultJson = """
        {
          "formOrder": [
            "personal_details",
            "address_details",
            "financial_information",
            "review_submit"
          ],
          "formStatus": {
            "personal_details": {
              "status": "notfilled"
            },
            "address_details": {
              "status": "notfilled",
              "formData": {
                "form.address.streetAddress": "Abc road",
                "form.address.city": "Bengaluru",
                "form.address.state": "KARNATAKA",
                "form.address.zipCode": "001100",
                "form.address.residenceType": "Resident"
              }
            },
            "financial_information": {
              "status": "notfilled"
            },
            "review_submit": {
              "status": "pendingForSubmit",
              "formNeedTobeDeleteAfterSubmit": [
                "personal_details",
                "address_details",
                "financial_information"
              ]
            }
          }
        }
    """.trimIndent()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Match the endpoint for form status
        if (url.contains("formStatus")) {
            return when (request.method) {
                "GET" -> {
                    val currentData = prefs.getString("form_status_json", defaultJson) ?: defaultJson
                    createResponse(chain, currentData)
                }
                "POST" -> {
                    val bodyString = request.body?.let { body ->
                        val buffer = okio.Buffer()
                        body.writeTo(buffer)
                        buffer.readUtf8()
                    }
                    
                    if (!bodyString.isNullOrEmpty()) {
                        // In a real scenario, you'd merge or update specific fields.
                        // Here we simulate updating the entire status based on the POST body.
                        prefs.edit { putString("form_status_json", bodyString) }
                    }
                    
                    // Return the updated data to confirm
                    createResponse(chain, bodyString ?: "{}")
                }
                else -> chain.proceed(request)
            }
        }

        return chain.proceed(request)
    }

    private fun createResponse(chain: Interceptor.Chain, json: String): Response {
        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .addHeader("content-type", "application/json")
            .build()
    }
}
