package com.linecorp.interview.movie.app.screens.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.linecorp.android.extensions.gone
import com.linecorp.android.extensions.rxOnClick
import com.linecorp.android.extensions.visible
import com.linecorp.android.libs.imageloader.CacheStrategy
import com.linecorp.android.libs.imageloader.ImageLoader
import com.linecorp.android.screens.fragments.BindingCoreFragment
import com.linecorp.interview.movie.app.R
import com.linecorp.interview.movie.app.databinding.FragmentMainBinding
import com.linecorp.interview.movie.app.vm.MainViewModel
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : BindingCoreFragment<FragmentMainBinding>(R.layout.fragment_main) {

    // region [Inject]
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: MainViewModel by activityViewModels { viewModelFactory }
    // endregion

    private var currentIndex = 0
    private var images = listOf<String>()

    override fun onSyncEvents() {
        super.onSyncEvents()
        binding.imageView.rxOnClick { moveNext() }
        viewModel.movie.observe(viewLifecycleOwner) {
            images = it.images
            binding.textTitle.text = it.title
            currentIndex = 0
            loadImage()
        }
    }

    override fun onSyncData() {
        super.onSyncData()
        viewModel.getMove()
    }

    private fun moveNext() {
        if (currentIndex + 1 >= images.size) {
            currentIndex = 0
        } else {
            currentIndex++
        }
        loadImage()
    }

    private fun loadImage() {
        ImageLoader.getInstance(requireContext())
            .cacheStrategy(CacheStrategy.DISK)
            .onProgress {
                if (it == Long.MAX_VALUE) {
                    binding.textView.gone()
                } else {
                    binding.textView.text = String.format("%s kb downloading", it)
                    binding.textView.visible()
                }
            }
            .load(binding.imageView, images[currentIndex])
    }
}