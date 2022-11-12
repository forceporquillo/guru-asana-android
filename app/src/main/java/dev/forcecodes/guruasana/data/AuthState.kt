package dev.forcecodes.guruasana.data

import dev.forcecodes.guruasana.android.data.State

sealed class AuthState : State {
    object SignedOut : AuthState()
    object SignedIn : AuthState()
}