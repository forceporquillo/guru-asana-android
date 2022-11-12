package dev.forcecodes.guruasana.widget

data class SnackbarMessage(
    /** Resource string ID of the message to show */
    val messageId: Int? = null,

    val indefinite: Boolean = false,

    /** Optional change ID to avoid repetition of messages */
    val requestChangeId: String? = null,

    val message: String? = null
)
