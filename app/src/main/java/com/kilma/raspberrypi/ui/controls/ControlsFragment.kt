package com.kilma.raspberrypi.ui.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kilma.raspberrypi.R

class ControlsFragment : Fragment() {

    private lateinit var viewModel: ControlsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ControlsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }
}
