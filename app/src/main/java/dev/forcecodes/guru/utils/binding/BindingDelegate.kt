package dev.forcecodes.guru.utils.binding

import androidx.viewbinding.ViewBinding

interface BindingDelegate<VB: ViewBinding> {
    fun binding(lambda: VB.() -> Unit)
}