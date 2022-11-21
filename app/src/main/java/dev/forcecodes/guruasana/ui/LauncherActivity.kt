package dev.forcecodes.guruasana.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.ui.auth.AuthActivity
import dev.forcecodes.guruasana.databinding.ActivityLauncherBinding
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