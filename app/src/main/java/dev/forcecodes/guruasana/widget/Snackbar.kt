package dev.forcecodes.guruasana.widget

import androidx.fragment.app.Fragment
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.utils.extensions.launchWithViewLifecycle

/**
 * An extension for Fragments that sets up a Snackbar with a [SnackbarMessageManager].
 */
fun Fragment.setupSnackbarManager(
    snackbarMessageManager: SnackbarMessageManager
) {
    launchWithViewLifecycle {
        snackbarMessageManager.currentSnackbar.collect { message ->
            if (message == null || message.message.isNullOrEmpty()) {
                return@collect
            }

            val messageText = message.message

            val fadingSnackbar = view?.findViewById<FadingSnackbar>(R.id.snackbar)
            fadingSnackbar?.show(
                messageText = messageText,
                actionId = null,
                longDuration = false,
                indefinite = message.indefinite,
                actionClick = {
                    fadingSnackbar.dismiss()
                },
                // When the snackbar is dismissed, ping the snackbar message manager in case there
                // are pending messages.
                dismissListener = {
                    snackbarMessageManager.removeMessageAndLoadNext(message)
                }
            )
        }
    }
}
