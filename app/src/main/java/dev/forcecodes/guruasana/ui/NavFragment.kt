package dev.forcecodes.guruasana.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.utils.extensions.setupToolbarNavigateUp

abstract class NavFragment(@LayoutRes open val layoutId: Int) : Fragment(layoutId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbarNavigateUp(view.findViewById(R.id.toolbar) ?: return)
    }
}