@file:Suppress("deprecation")

package dev.forcecodes.guruasana.utils

import android.app.ProgressDialog
import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import dev.forcecodes.guruasana.R

@Suppress("WeakerAccess")
object ProgressDialogUtil {

    private var progressDialog: ProgressDialog? = null

    private fun initDialog(show: Boolean, context: Context, @StringRes resId: Int) {
        if (progressDialog == null) {
            progressDialog =
                ProgressDialog(context, R.style.ProgressDialogStyle).apply {
                    setMessage(context.getString(resId))
                    setCancelable(false)
                }
        }
        if (!show) return
        show()
    }

    @MainThread
    @JvmStatic
    fun show() {
        progressDialog?.show()
    }

    @MainThread
    @JvmStatic
    fun hide() {
        if (isShowing) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }

    @MainThread
    @JvmStatic
    fun showOrDismiss(
        show: Boolean,
        context: Context,
        @StringRes resId: Int = R.string.progress_loading_message
    ) {
        if (progressDialog == null && !isShowing) {
            initDialog(show, context, resId)
        } else {
            if (!show) {
                hide()
            }
        }
    }

    private val isShowing: Boolean
        get() = progressDialog?.isShowing == true
}

inline fun Fragment.setupLoadingState(
    value: Boolean?,
    @StringRes block: () -> Int = { R.string.progress_loading_message }
) {
    ProgressDialogUtil.showOrDismiss(value ?: return, requireActivity(), block.invoke())
}