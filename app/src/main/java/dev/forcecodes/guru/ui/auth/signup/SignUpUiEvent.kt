package dev.forcecodes.guru.ui.auth.signup

import dev.forcecodes.guru.ui.UiEvent

sealed class SignUpUiEvent : UiEvent {
    object OnSignUp : SignUpUiEvent()
    object OnDismissState : SignUpUiEvent()
}