package dev.forcecodes.guru.utils.extensions

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

fun Fragment.setupToolbarNavigateUp(toolbar: Toolbar) {
    toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
}