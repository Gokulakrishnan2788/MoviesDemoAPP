package com.example.moviesdemoapp.core.network

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Retrofit [Converter.Factory] backed by Kotlinx Serialization [Json].
 * Replaces the Jake Wharton adapter without adding an extra dependency.
 */
@OptIn(InternalSerializationApi::class)
class KotlinxSerializationConverterFactory private constructor(
    private val json: Json,
) : Converter.Factory() {

    private val contentType = "application/json; charset=utf-8".toMediaType()

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val deserializer = json.serializersModule.serializer(type)
        return Converter<ResponseBody, Any> { body ->
            json.decodeFromString(deserializer, body.string())
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody> {
        val serializer = json.serializersModule.serializer(type)
        return Converter<Any, RequestBody> { value ->
            json.encodeToString(serializer, value).toRequestBody(contentType)
        }
    }

    companion object {
        /** Create a factory wrapping the given [json] instance. */
        fun create(json: Json): KotlinxSerializationConverterFactory =
            KotlinxSerializationConverterFactory(json)
    }
}
