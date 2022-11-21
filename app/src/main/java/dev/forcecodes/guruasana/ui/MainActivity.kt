package dev.forcecodes.guruasana.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.guruasana.R
import dev.forcecodes.guruasana.data.auth.UserCredentials
import dev.forcecodes.guruasana.data.auth.UserDataSource
import dev.forcecodes.guruasana.databinding.ActivityMainBinding
import dev.forcecodes.guruasana.utils.binding.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnBackPressListener {

    private lateinit var navController: NavController
    private val binding by viewBinding(ActivityMainBinding::inflate)

    @Inject
    lateinit var authDataSource: UserDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_content_main)

        lifecycleScope.launchWhenCreated {
            FirebaseFirestore.getInstance()
                .collection("username")
                .document("aljan")
                .get().await()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }

    override fun onNavigateBack() {
        navController.navigateUp()
    }
}