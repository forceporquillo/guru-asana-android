package dev.forcecodes.guru.ui.auth.login

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.guru.data.auth.UserCredentials
import dev.forcecodes.guru.domain.usecase.LoginUseCase
import dev.forcecodes.guru.logger.Logger
import dev.forcecodes.guru.ui.ReactiveStateViewModel
import dev.forcecodes.guru.ui.UiEvent
import dev.forcecodes.guru.ui.UiState
import dev.forcecodes.guru.ui.auth.signup.SignUpUiFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ReactiveStateViewModel<LoginUiState, LoginUiEvent>(
    initialState = LoginUiState.signedOut()
) {

    val userId = MutableStateFlow("")
    val password = MutableStateFlow("")

    override fun stateReducer(oldState: LoginUiState, event: LoginUiEvent) {
        if (event is LoginUiEvent.Login) {
            viewModelScope {
                loginUseCase.invoke(
                    LoginUseCase.Params(createCredentials)
                ).collect { state -> setState(state) }
            }
        }
    }

    private val createCredentials: UserCredentials
        get() {
            val validEmail = SignUpUiFormValidator.isValidEmail(this.userId.value)
            return UserCredentials(
                username = if (validEmail) "" else this.userId.value,
                email = if (validEmail) this.userId.value else "",
                password = this.password.value
            )
        }

    fun onClickLogin() {
        sendEvent(LoginUiEvent.Login)
    }

    fun onClickCreate() {
        sendEvent(LoginUiEvent.Create)
    }

    init {
        viewModelScope {
            combine(userId, password) { userId, password ->

                state.value.copy(
                    enableSubmitButton = userId.isNotEmpty()
                            && password.length >= 6,
                )
            }.collect { state -> setState(state) }
        }
    }
}

data class LoginUiState(
    val isAuthenticated: Boolean,
    val enableSubmitButton: Boolean,
    val isLoading: Boolean? = null,
    val error: String? = null
) : UiState {

    companion object {
        fun signedOut(): LoginUiState {
            return LoginUiState(
                isAuthenticated = false,
                enableSubmitButton = false
            )
        }
    }
}

sealed class LoginUiEvent : UiEvent {
    object Login : LoginUiEvent()
    object Create : LoginUiEvent()
}