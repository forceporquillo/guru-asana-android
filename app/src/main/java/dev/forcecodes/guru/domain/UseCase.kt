@file:Suppress("unused")

package dev.forcecodes.guru.domain

import dev.forcecodes.guru.data.Result
import dev.forcecodes.guru.ui.UiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface UseCaseParams {
    abstract class Params
}

/**
 * Executes business logic synchronously using Coroutines.
 */
abstract class FlowUseCase<in P : UseCaseParams.Params, R>(
    coroutineDispatcher: CoroutineDispatcher
) : BaseFlowUseCase<P, Result<R>>(coroutineDispatcher), UseCaseParams {

    /**
     * Executes the use case synchronously and returns a Flow [Result].
     *
     * @return a Flow [Result].
     *
     * @param parameters the input parameters to run the use case with
     */

    abstract override fun execute(params: P): Flow<Result<R>>

    override fun mapExceptionToResult(throwable: Throwable): Result<R> {
        return Result.Error(throwable.message)
    }
}

/**
 * Executes business logic synchronously using Coroutines and returns the UI state.
 */
abstract class UiStateFlowUseCase<in P : UseCaseParams.Params, R : UiState>(
    coroutineDispatcher: CoroutineDispatcher
) : BaseFlowUseCase<P, R>(coroutineDispatcher), UseCaseParams {

    /**
     * Executes the use case synchronously and returns a Flow [Result].
     *
     * @return a Flow [Result].
     *
     * @param parameters the input parameters to run the use case with
     */

    abstract override fun execute(params: P): Flow<R>

    override fun mapExceptionToResult(throwable: Throwable): R {
       throw UnsupportedOperationException()
    }
}

abstract class BaseFlowUseCase<in P : UseCaseParams.Params, R>(
    private val coroutineDispatcher: CoroutineDispatcher
) : UseCaseParams {
    /**
     * Executes the use case synchronously and returns a Flow [Result].
     *
     * @return a Flow [Result].
     *
     * @param parameters the input parameters to run the use case with
     */
    operator fun invoke(params: P): Flow<R> = execute(params)
        .catch { e -> emit(mapExceptionToResult(e)) }
        .flowOn(coroutineDispatcher)

    protected abstract fun execute(params: P): Flow<R>

    protected abstract fun mapExceptionToResult(throwable: Throwable): R
}

/**
 * Executes business logic synchronously or asynchronously using Coroutines.
 */
abstract class UseCase<in P : UseCaseParams.Params, R>(
    private val coroutineDispatcher: CoroutineDispatcher
) : UseCaseParams {

    /**
     * Executes the use case asynchronously and returns a [Result].
     *
     * @return a [Result].
     *
     * @param parameters the input parameters to run the use case with
     */
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters).let {
                    Result.Success(it)
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Override this to set the code to be executed.
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): R
}