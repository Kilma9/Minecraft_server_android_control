package com.kilma.raspberrypi.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kilma.raspberrypi.R

class ChatFragment : Fragment() {

    private lateinit var viewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
}
