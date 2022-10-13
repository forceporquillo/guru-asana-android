package dev.forcecodes.guru.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import dev.forcecodes.guru.R
import dev.forcecodes.guru.R.id
import dev.forcecodes.guru.databinding.ActivityMainBinding
import dev.forcecodes.guru.utils.binding.viewBinding

class MainActivity : AppCompatActivity(), OnBackPressListener {

  private lateinit var appBarConfiguration: AppBarConfiguration
  private lateinit var navController: NavController
  private val binding by viewBinding(ActivityMainBinding::inflate)

  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)

    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)

    navController = findNavController(id.nav_host_fragment_content_main)
    appBarConfiguration = AppBarConfiguration(navController.graph)
    setupActionBarWithNavController(navController, appBarConfiguration)
  }

  override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp(appBarConfiguration)
      || super.onSupportNavigateUp()
  }

  override fun onNavigateBack() {
    navController.navigateUp()
  }
}