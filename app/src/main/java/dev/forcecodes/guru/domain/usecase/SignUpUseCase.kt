package dev.forcecodes.guru.domain.usecase

import dev.forcecodes.guru.data.Result
import dev.forcecodes.guru.data.auth.UserCredentials
import dev.forcecodes.guru.data.auth.UserDataSource
import dev.forcecodes.guru.di.IoDispatcher
import dev.forcecodes.guru.domain.UiStateFlowUseCase
import dev.forcecodes.guru.domain.UseCaseParams
import dev.forcecodes.guru.ui.auth.signup.SignUpUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignUpUseCase @Inject constructor(
    private val authDataSource: UserDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UiStateFlowUseCase<SignUpUseCase.Params, SignUpUiState>(ioDispatcher) {

    data class Params(val credentials: UserCredentials) : UseCaseParams.Params()

    override fun execute(params: Params): Flow<SignUpUiState> {
        return authDataSource.signup(params.credentials).map { result ->
            when (result) {
                is Result.Loading -> {
                    SignUpUiState(
                        isLoading = true,
                        isSuccess = false,
                        enableSubmitButton = false
                    )
                }
                is Result.Success -> {
                    SignUpUiState(
                        isLoading = false,
                        isSuccess = result.data.getUid() != null,
                        enableSubmitButton = false
                    )
                }
                is Result.Error -> {
                    SignUpUiState(
                        isLoading = false,
                        isSuccess = false,
                        enableSubmitButton = true,
                        error = result.exception
                    )
                }
            }
        }
    }
}