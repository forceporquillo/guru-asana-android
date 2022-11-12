/*
 * Copyright 2021 strongforce1
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package dev.forcecodes.guruasana.domain

import dev.forcecodes.guruasana.data.Result
import dev.forcecodes.guruasana.ui.UiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Marker interface for all business logic uses cases
 */
interface UseCaseParams {

    /**
     * Marker interface for use case parameters.
     */
    abstract class Params
}

/**
 * Executes business logic synchronously or asynchronously.
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

/**
 * Executes business logic synchronously and returns the corresponding [UiState].
 */
abstract class UiStateFlowUseCase<in P : UseCaseParams.Params, R : UiState>(
    coroutineDispatcher: CoroutineDispatcher
) : BaseFlowUseCase<P, R>(coroutineDispatcher), UseCaseParams {

    /**
     * Executes the use case synchronously and returns a Flow [Result].
     *
     * @return a Flow [UiState].
     *
     * @param params the input parameters to run the use case with
     */

    abstract override fun execute(params: P): Flow<R>

    override fun mapExceptionToResult(throwable: Throwable): R {
        throw UnsupportedOperationException()
    }
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
     * @param params the input parameters to run the use case with
     */

    abstract override fun execute(params: P): Flow<Result<R>>

    override fun mapExceptionToResult(throwable: Throwable): Result<R> {
        return Result.Error(throwable.message)
    }
}

/**
 * Base class for all business logic that executes within the [Flow] scope.
 */
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