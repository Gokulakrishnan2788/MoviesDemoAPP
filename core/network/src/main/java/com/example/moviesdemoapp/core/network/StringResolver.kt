package com.example.moviesdemoapp.core.network

/**
 * Abstraction for resolving localized strings by key.
 *
 * Lives in core:network so engine:sdui and core:data can depend on it
 * without introducing any Android-framework import into pure core modules.
 *
 * The Android implementation (AndroidStringResolver) lives in engine:sdui
 * and is wired via a Hilt module — core never knows about Context.
 */
interface StringResolver {
    /**
     * Return the string value for [key].
     * Returns [key] itself when no matching resource is found — never null,
     * never throws.
     */
    fun resolve(key: String): String
}
