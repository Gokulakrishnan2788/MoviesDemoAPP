package com.example.moviesdemoapp.core.domain

/** Wraps a successful value, a loading state, or an error. */
sealed class Result<out T> {
    /** Successful outcome containing [data]. */
    data class Success<T>(val data: T) : Result<T>()

    /** Failed outcome with optional HTTP [code] and human-readable [message]. */
    data class Error(val code: Int? = null, val message: String) : Result<Nothing>()

    /** In-progress operation. */
    object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(data)
    return this
}

inline fun <T> Result<T>.onError(block: (code: Int?, message: String) -> Unit): Result<T> {
    if (this is Result.Error) block(code, message)
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> Result.Loading
}
