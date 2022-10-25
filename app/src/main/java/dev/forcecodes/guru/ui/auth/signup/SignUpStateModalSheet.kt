package dev.forcecodes.guru.ui.auth.signup

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.forcecodes.guru.R
import dev.forcecodes.guru.utils.extensions.navigateUp

class SignUpStateModalSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<SignUpViewModel>({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_bottom_sheet_signup_state, container, false)

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onHandleDismissModalEvent()
        requireParentFragment().navigateUp()
    }

    companion object {
        const val TAG = "SignUpStateModalSheet"
    }
}