package com.linecorp.interview.movie.app.screens.activities

import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.linecorp.android.libs.imageloader.ImageLoader
import com.linecorp.android.screens.activities.BindingCoreActivity
import com.linecorp.interview.movie.app.R
import com.linecorp.interview.movie.app.databinding.ActivityMainBinding
import com.linecorp.interview.movie.app.vm.MainViewModel
import javax.inject.Inject

class MainActivity : BindingCoreActivity<ActivityMainBinding>(R.layout.activity_main) {

    // region [Inject]
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel by viewModels { viewModelFactory }
    // endregion

    private lateinit var navController: NavController
    private lateinit var imageLoader: ImageLoader

    override fun onSyncViews() {
        super.onSyncViews()
        imageLoader = ImageLoader.getInstance(this)
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onPause() {
        super.onPause()
        imageLoader.flush()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageLoader.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

}