package dev.forcecodes.guru.utils.binding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

inline fun <T : ViewBinding> Fragment.viewBinding(
    crossinline viewBindingFactory: (View) -> T
): FragmentViewBindingDelegate<T> =
    FragmentViewBindingDelegate(this) { delegate ->
        viewBindingFactory(delegate.requireView())
    }

inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(
    noinline bindingInflater: (LayoutInflater) -> T
) = ActivityViewBindingDelegate(this, bindingInflater)

/**
 * TypeAlias for inflating a [ViewBinding] delegate as lambda.
 */
typealias BindingInflater<T> = (LayoutInflater, ViewGroup, Boolean) -> T

inline fun <T : ViewBinding> ViewGroup.viewBinding(
    crossinline factory: BindingInflater<T>,
    attach: Boolean = false
): T = factory(LayoutInflater.from(context), this, attach)

inline fun <T : ViewBinding> Fragment.viewInflateBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
): FragmentViewBindingDelegate<T> =
    FragmentViewBindingDelegate(this) { delegate ->
        bindingInflater(delegate.layoutInflater)
    }

fun <R : AppCompatActivity, T : ViewDataBinding> contentView(
    @LayoutRes layoutRes: Int
): ContentViewBindingDelegate<R, T> = ContentViewBindingDelegate(layoutRes)

