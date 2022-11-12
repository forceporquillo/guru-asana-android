package dev.forcecodes.guruasana.widget

import androidx.annotation.VisibleForTesting
import dev.forcecodes.guruasana.di.ApplicationScope
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.widget.SnackbarMessageManager.Companion.MAX_ITEMS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A single source of Snackbar messages related to reservations.
 *
 * Only shows one Snackbar related to one change across all screens
 *
 * Emits new values on request (when a Snackbar is dismissed and ready to show a new message)
 *
 * It keeps a list of [MAX_ITEMS] items, enough to figure out if a message has already been shown,
 * but limited to avoid wasting resources.
 *
 * *Note:* This source code was modified to suit PregGo's app use-cases.
 *
 */
@Singleton
open class SnackbarMessageManager @Inject constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope
) {
    companion object {
        // Keep a fixed number of old items
        @VisibleForTesting
        const val MAX_ITEMS = 100
    }

    private val messages = mutableListOf<SnackbarMessage>()

    private val _currentSnackbar = MutableStateFlow<SnackbarMessage?>(null)
    val currentSnackbar: StateFlow<SnackbarMessage?> = _currentSnackbar

    fun addMessage(msg: SnackbarMessage) {
        coroutineScope.launch {
            // Limit amount of pending messages
            if (messages.size > MAX_ITEMS) {
                Logger.e("Too many Snackbar messages. Message id: ${msg.messageId}")
                return@launch
            }
            // If the new message is about the same change as a pending one, keep the old one. (rare)
            val sameRequestId = messages.find {
                it.requestChangeId == msg.requestChangeId
            }
            if (sameRequestId == null) {
                messages.add(msg)
            }
            loadNext()
        }
    }

    private fun loadNext() {
        if (_currentSnackbar.value == null) {
            _currentSnackbar.value = messages.firstOrNull()
        }
    }

    fun removeMessageAndLoadNext(shownMsg: SnackbarMessage?) {
        messages.removeAll { it == shownMsg }
        if (_currentSnackbar.value == shownMsg) {
            _currentSnackbar.value = null
        }
        loadNext()
    }
}