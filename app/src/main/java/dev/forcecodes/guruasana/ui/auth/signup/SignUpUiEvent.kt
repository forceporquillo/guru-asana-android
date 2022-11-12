package dev.forcecodes.guruasana.ui.auth.signup

import dev.forcecodes.guruasana.ui.UiEvent

sealed class SignUpUiEvent : UiEvent {
    object OnSignUp : SignUpUiEvent()
    object OnDismissState : SignUpUiEvent()
}