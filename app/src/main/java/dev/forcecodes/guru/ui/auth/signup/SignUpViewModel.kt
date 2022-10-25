package dev.forcecodes.guru.ui.auth.signup

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.guru.data.auth.UserCredentials
import dev.forcecodes.guru.domain.usecase.SignUpUseCase
import dev.forcecodes.guru.logger.Logger
import dev.forcecodes.guru.ui.ReactiveStateViewModel
import dev.forcecodes.guru.utils.extensions.cancelIfActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ReactiveStateViewModel<SignUpUiState, SignUpUiEvent>(
    initialState = SignUpUiState.unauthenticated()
) {

    // region StateFlow
    val username = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")
    // endregion

    private var signUpJob: Job? = null
    private val signUpValidator = SignUpUiFormValidator

    fun onHandleSignUpEvent() {
        super.sendEvent(SignUpUiEvent.OnSignUp)
    }

    fun onHandleDismissModalEvent() {
        super.sendEvent(SignUpUiEvent.OnDismissState)
    }

    override fun stateReducer(oldState: SignUpUiState, event: SignUpUiEvent) {
        // cancel any outgoing enlist state process-sync with
        // the server for every event it triggers.
        signUpJob?.cancelIfActive()
        if (event is SignUpUiEvent.OnSignUp) {
            signUpJob = viewModelScope {
                signUpUseCase.invoke(
                    SignUpUseCase.Params(
                        UserCredentials(
                            username = username.value,
                            email = email.value,
                            password = password.value
                        )
                    )
                ).collect { state -> setState(state) }
            }
        }
        if (event is SignUpUiEvent.OnDismissState) {
            // reset state
            setState(SignUpUiState.unauthenticated())
        }
    }

    private fun setSideEffect(enable: Boolean) {
        val oldState = state.value
        super.setState(oldState.copy(enableSubmitButton = enable))
    }

    init {
        viewModelScope {
            combine(
                username,
                email,
                password,
                confirmPassword
            ) { username, email, password, confirmPassword ->
                Logger.d("State: $username $email $password $confirmPassword")
                username.isNotEmpty() &&
                        signUpValidator.validateBasicCredentials(
                            email,
                            password,
                            confirmPassword
                        )
            }.collect(::setSideEffect)
        }
    }
}