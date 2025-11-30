package com.kilma.minecraft.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.kilma.minecraft.R
import com.kilma.minecraft.data.ChatMessage
import com.kilma.minecraft.rcon.RconManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var recyclerChat: RecyclerView
    private lateinit var editMessage: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var btnFilter30Min: Button
    private lateinit var btnFilter1H: Button
    private lateinit var btnFilter4H: Button
    private lateinit var btnFilter1Day: Button

    private var selectedFilterMinutes = 60 // Default 1 hour

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Initialize views
        recyclerChat = view.findViewById(R.id.recycler_chat)
        editMessage = view.findViewById(R.id.edit_chat_message)
        btnSend = view.findViewById(R.id.btn_send_message)
        btnFilter30Min = view.findViewById(R.id.btn_filter_30min)
        btnFilter1H = view.findViewById(R.id.btn_filter_1h)
        btnFilter4H = view.findViewById(R.id.btn_filter_4h)
        btnFilter1Day = view.findViewById(R.id.btn_filter_1day)

        // Setup RecyclerView
        chatAdapter = ChatAdapter()
        recyclerChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

        // Setup filter buttons
        btnFilter30Min.setOnClickListener { applyFilter(30) }
        btnFilter1H.setOnClickListener { applyFilter(60) }
        btnFilter4H.setOnClickListener { applyFilter(240) }
        btnFilter1Day.setOnClickListener { applyFilter(1440) }

        // Setup send button
        btnSend.setOnClickListener { sendMessage() }

        // Set default filter
        applyFilter(60)

        // Load initial chat history
        loadChatHistory()

        return view
    }

    private fun applyFilter(minutes: Int) {
        selectedFilterMinutes = minutes

        // Update button states
        btnFilter30Min.alpha = if (minutes == 30) 1.0f else 0.6f
        btnFilter1H.alpha = if (minutes == 60) 1.0f else 0.6f
        btnFilter4H.alpha = if (minutes == 240) 1.0f else 0.6f
        btnFilter1Day.alpha = if (minutes == 1440) 1.0f else 0.6f

        loadChatHistory()
    }

    private fun loadChatHistory() {
        // TODO: Integrate with RCON to fetch actual chat logs
        // For now, using mock data
        val currentTime = System.currentTimeMillis()
        val filterTime = currentTime - (selectedFilterMinutes * 60 * 1000)

        val mockMessages = listOf(
            ChatMessage(currentTime - 10000, "Player1", "Hello everyone!", false),
            ChatMessage(currentTime - 120000, "Player2", "Anyone want to trade?", false),
            ChatMessage(currentTime - 300000, "Server", "Player3 joined the game", true),
            ChatMessage(currentTime - 600000, "Player3", "Thanks for the tour!", false),
            ChatMessage(currentTime - 900000, "Player1", "Let's build a castle", false),
            ChatMessage(currentTime - 1800000, "Player2", "Found diamonds!", false),
            ChatMessage(currentTime - 3600000, "Server", "Server restarted", true),
            ChatMessage(currentTime - 7200000, "Player1", "Good morning!", false)
        )

        // Filter messages by selected time range
        val filteredMessages = mockMessages.filter { it.timestamp >= filterTime }

        chatAdapter.submitList(filteredMessages)

        // Scroll to bottom
        if (filteredMessages.isNotEmpty()) {
            recyclerChat.scrollToPosition(filteredMessages.size - 1)
        }
    }

    private fun sendMessage() {
        val message = editMessage.text?.toString()?.trim()
        if (message.isNullOrEmpty()) return
        
        if (!RconManager.isConnected()) {
            Toast.makeText(requireContext(), "Not connected to server", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            // Send message as server broadcast using "say" command
            val command = "say [App] $message"
            val result = RconManager.sendCommand(command)
            
            if (result.isSuccess) {
                editMessage.text?.clear()
                Toast.makeText(requireContext(), "✓ Message sent", Toast.LENGTH_SHORT).show()
                // Note: Real chat history would need server-side logging plugin
                // RCON doesn't provide chat history retrieval
            } else {
                Toast.makeText(requireContext(), "Failed to send: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Chat Adapter
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var messages = listOf<ChatMessage>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timestampText: android.widget.TextView = view.findViewById(R.id.chat_timestamp)
        private val playerText: android.widget.TextView = view.findViewById(R.id.chat_player)
        private val messageText: android.widget.TextView = view.findViewById(R.id.chat_message)

        fun bind(chatMessage: ChatMessage) {
            timestampText.text = dateFormat.format(Date(chatMessage.timestamp))
            playerText.text = chatMessage.player

            if (chatMessage.isServerMessage) {
                playerText.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            } else {
                playerText.setTextColor(itemView.context.getColor(android.R.color.holo_blue_dark))
            }

            messageText.text = chatMessage.message
        }
    }
}
