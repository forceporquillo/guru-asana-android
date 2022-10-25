package dev.forcecodes.guru.data

sealed class AuthState : State {
    object SignedOut : AuthState()
    object SignedIn : AuthState()
}