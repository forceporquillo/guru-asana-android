package dev.forcecodes.guru.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.ActivityLauncherBinding
import dev.forcecodes.guru.ui.auth.AuthActivity
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}

class LauncherViewModel @Inject constructor(
) : ViewModel() {

}