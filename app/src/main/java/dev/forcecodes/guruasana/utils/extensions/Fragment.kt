package dev.forcecodes.guruasana.utils.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Fragment.setupToolbarNavigateUp(toolbar: Toolbar) {
    toolbar.setNavigationOnClickListener { navigateUp() }
}

fun Fragment.navigateUp() {
    findNavController().navigateUp()
}

fun Fragment.navigate(@IdRes resId: Int, args: Bundle? = null) {
    findNavController().navigate(resId, args)
}

fun Fragment.navigate(route: String) {
    findNavController().navigate(route)
}

fun Fragment.launchWithViewLifecycle(
    delayInMillis: Long = 0L,
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend () -> Unit
): Job {
    return viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(activeState) {
            delay(delayInMillis); block()
        }
    }
}