package com.example.moviesdemoapp.engine.sdui

import android.content.Context
import com.example.moviesdemoapp.core.network.StringResolver

/**
 * Android implementation of [StringResolver].
 *
 * Key format rule: JSON binding keys use dot notation ("series_detail.synopsis_title")
 * while Android string resources use underscores ("series_detail_synopsis_title").
 * This class converts automatically before calling [Context.getIdentifier].
 *
 * Falls back to the raw [key] when no matching string resource is found,
 * so missing keys are visible in the UI instead of crashing.
 */
class AndroidStringResolver(private val context: Context) : StringResolver {

    override fun resolve(key: String): String {
        val resourceKey = key.replace('.', '_')
        val resId = context.resources.getIdentifier(resourceKey, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }
}
