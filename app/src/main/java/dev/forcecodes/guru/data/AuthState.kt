package dev.forcecodes.guru.data

import dev.forcecodes.preggo.auth.State

sealed class AuthState : State {
    object SignedOut : AuthState()
    object SignedIn : AuthState()
}