@file:Suppress("unused")

package dev.forcecodes.guruasana.utils.binding

import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import dev.forcecodes.guruasana.logger.Logger
import kotlin.reflect.KProperty

abstract class LifecycleViewBindingProperty<in R : Any, out T : ViewBinding>(
    private val viewBinder: (R) -> T
) : ViewBindingProperty<R, T> {

    private var viewBinding: T? = null

    protected abstract fun getLifecycleOwner(thisRef: R): LifecycleOwner

    @MainThread
    override fun getValue(thisRef: R, property: KProperty<*>): T {
        viewBinding?.let { return it }

        val lifecycle = getLifecycleOwner(thisRef).lifecycle
        val viewBinding = viewBinder(thisRef)
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            Logger.e("Access to viewBinding after Lifecycle is destroyed or hasn't created yet. The instance of viewBinding will be not cached.")
            // We can access to ViewBinding after Fragment.onDestroyView(), but don't save it to prevent memory leak
        } else {
            lifecycle.addObserver(ClearOnDestroyLifecycleObserver(this))
            this.viewBinding = viewBinding
        }
        return viewBinding
    }

    @MainThread
    @CallSuper
    override fun clear() {
        viewBinding = null
    }

    internal fun postClear() {
        if (!mainHandler.post { clear() }) {
            clear()
        }
    }

    private class ClearOnDestroyLifecycleObserver(
        private val property: LifecycleViewBindingProperty<*, *>
    ) : DefaultLifecycleObserver {

        @MainThread
        override fun onDestroy(owner: LifecycleOwner) {
            property.postClear()
        }
    }

    private companion object {

        private val mainHandler = Handler(Looper.getMainLooper())
    }
}