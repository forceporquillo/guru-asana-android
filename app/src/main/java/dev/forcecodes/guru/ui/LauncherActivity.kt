package dev.forcecodes.guru.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import dev.forcecodes.guru.R
import dev.forcecodes.guru.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

class LauncherViewModel : ViewModel() {

}