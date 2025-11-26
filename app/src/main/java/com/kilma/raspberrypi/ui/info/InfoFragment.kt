package com.kilma.raspberrypi.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kilma.raspberrypi.R

class InfoFragment : Fragment() {

    private lateinit var viewModel: InfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(InfoViewModel::class.java)
        return inflater.inflate(R.layout.fragment_info, container, false)
    }
}
