package dev.forcecodes.guruasana.utils.extensions

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import dev.forcecodes.guruasana.logger.Logger

fun View.onNavigateWhenInvoked(route: String) {
    val fragment = findFragment<Fragment>()
    setOnClickListener {
        Logger.d("onNavigateWhenInvoked: $route")
        fragment.findNavController().navigate(route)
    }
}

fun <T : ViewBinding> T.onNavigateWhenInvoked(route: String) {
    root.onNavigateWhenInvoked(route)
}
