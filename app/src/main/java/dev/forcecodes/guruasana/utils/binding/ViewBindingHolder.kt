package dev.forcecodes.guruasana.utils.binding

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

/**
 * Interface class which used to register the delegate
 * [ViewBinding] that is tied to the given [LifecycleOwner].
 */
interface ViewBindingHolder<B : ViewBinding> {

    var binding: B?

    // Only valid between onCreateView and onDestroyView.
    fun requireBinding() = checkNotNull(binding)

    /**
     * Convenient binding calls.
     *
     * Same to scoping functions -> Foo.let{}
     */
    fun requireBindingLet(lambda: (B) -> Unit) {
        binding?.let {
            lambda(it)
        }
    }

    /**
     * Convenient binding calls.
     *
     * Same to scoping functions -> Foo.apply{}
     */
    fun requireBindingSelf(lambda: B.() -> Unit) {
        binding?.apply {
            lambda(this)
        }
    }

    /**
     * Registers the delegate [ViewBinding] upon lifecycle creation and clears the
     * [binding] instance whenever the [LifecycleOwner] called onDestroy for **Activity**
     * and onDestroyView for **Fragments**.
     *
     * @param binding: The [ViewBinding] type.
     * @param lifecycleOwner: The [LifecycleOwner] of the [ViewBinding].
     */
    fun registerBinding(
        binding: B,
        lifecycleOwner: LifecycleOwner
    ) {
        this.binding = binding
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                this@ViewBindingHolder.binding = null
            }
        })
    }

}