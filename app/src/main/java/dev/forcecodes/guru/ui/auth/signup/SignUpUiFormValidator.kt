package dev.forcecodes.guru.ui.auth.signup

import android.util.Patterns
import java.util.regex.Pattern

internal object SignUpUiFormValidator {

    private val passwordPattern =
        Pattern.compile("^[a-zA-Z0-9_@./#&+-]{6,}\$", Pattern.CASE_INSENSITIVE)

    fun atLeast6Characters(password: String): Boolean {
        return passwordPattern.matcher(password).matches()
    }

    fun isValidPassword(password: String, confirmPassword: String): Boolean {
        return if (password.isEmpty() || confirmPassword.isEmpty()) {
            false
        } else {
            password == confirmPassword
                    && atLeast6Characters(password)
        }
    }

    fun isPasswordMatch(password: String, confirmPassword: String): Boolean {
        return if (password.isEmpty() || confirmPassword.isEmpty()) {
            true
        } else {
            password == confirmPassword
        }
    }

    fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun validateBasicCredentials(vararg args: String): Boolean {
        return isValidEmail(args[0]) && isValidPassword(args[1], args[2])
                && isPasswordMatch(args[1], args[2])
    }
}
