package dev.forcecodes.guruasana.domain.usecase.auth

import dev.forcecodes.guruasana.data.Result
import dev.forcecodes.guruasana.data.auth.UserCredentials
import dev.forcecodes.guruasana.data.auth.UserDataSource
import dev.forcecodes.guruasana.di.IoDispatcher
import dev.forcecodes.guruasana.domain.UiStateFlowUseCase
import dev.forcecodes.guruasana.domain.UseCaseParams
import dev.forcecodes.guruasana.ui.auth.signup.SignUpUiState
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