package dev.forcecodes.guru.ui.auth

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.forcecodes.guru.R

class GuestLoginDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setIcon(R.drawable.ic_sync_problem)
            .setTitle(getString(R.string.guest_login_dialog_title))
            .setMessage(getString(R.string.guest_login_dialog_message))
            .setPositiveButton(getString(R.string.guest_login_dialog_positive_message)) { dialog, _ ->
                Toast.makeText(
                    requireContext(),
                    "TODO: Handle guest login state...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.guest_login_dialog_negative_message)) { dialog, _ -> dialog.cancel() }
            .show()
    }
}