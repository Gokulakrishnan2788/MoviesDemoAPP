package com.example.moviesdemoapp.core.domain

/**
 * Base class for all use cases. Each use case exposes exactly one [invoke] function.
 *
 * @param P parameter type passed to [invoke]
 * @param R return type from [invoke]
 */
abstract class BaseUseCase<in P, out R> {
    /** Execute the use case with [params] and return the result. */
    abstract suspend operator fun invoke(params: P): R
}
