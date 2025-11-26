package com.kilma.raspberrypi.ui.servers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kilma.raspberrypi.R

class ServersFragment : Fragment() {

    private lateinit var viewModel: ServersViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ServersViewModel::class.java)
        return inflater.inflate(R.layout.fragment_servers, container, false)
    }
}
