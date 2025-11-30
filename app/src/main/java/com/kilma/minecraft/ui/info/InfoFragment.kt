package com.kilma.minecraft.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kilma.minecraft.R
import com.kilma.minecraft.rcon.RconManager
import kotlinx.coroutines.launch

class InfoFragment : Fragment() {

    private lateinit var viewModel: InfoViewModel
    private lateinit var tvUptime: TextView
    private lateinit var tvOnlinePlayers: TextView
    private lateinit var tvTps: TextView
    private lateinit var tvMemory: TextView
    private lateinit var btnRefresh: Button
    private lateinit var recyclerPlayers: RecyclerView
    private lateinit var tvNoPlayers: TextView
    private lateinit var playersAdapter: ActivePlayersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(InfoViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_info, container, false)

        // Initialize views
        tvUptime = view.findViewById(R.id.tv_uptime)
        tvOnlinePlayers = view.findViewById(R.id.tv_online_players)
        tvTps = view.findViewById(R.id.tv_tps)
        tvMemory = view.findViewById(R.id.tv_memory)
        btnRefresh = view.findViewById(R.id.btn_refresh_stats)
        recyclerPlayers = view.findViewById(R.id.recycler_players)
        tvNoPlayers = view.findViewById(R.id.tv_no_players)

        // Setup RecyclerView
        playersAdapter = ActivePlayersAdapter()
        recyclerPlayers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = playersAdapter
        }

        // Setup refresh button
        btnRefresh.setOnClickListener { refreshStats() }

        // Load initial stats
        refreshStats()

        return view
    }

    private fun refreshStats() {
        if (!RconManager.isConnected()) {
            Toast.makeText(requireContext(), "Not connected to server", Toast.LENGTH_SHORT).show()
            // Show mock data
            showMockData()
            return
        }
        
        btnRefresh.isEnabled = false
        
        lifecycleScope.launch {
            // Get player list
            val listResult = RconManager.sendCommand("list")
            val players = mutableListOf<String>()
            var playerCount = 0
            
            if (listResult.isSuccess) {
                val response = listResult.getOrNull() ?: ""
                // Parse: "There are X of Y players online: player1, player2, ..."
                val match = Regex("""There are (\d+) of a maximum of (\d+) players online:""").find(response)
                if (match != null) {
                    playerCount = match.groupValues[1].toIntOrNull() ?: 0
                }
                
                val playersPart = response.substringAfter(":", "").trim()
                if (playersPart.isNotEmpty()) {
                    players.addAll(playersPart.split(",").map { it.trim() })
                }
            }
            
            // Get seed (just for demonstration)
            val seedResult = RconManager.sendCommand("seed")
            val seed = if (seedResult.isSuccess) {
                val seedResponse = seedResult.getOrNull() ?: ""
                seedResponse.substringAfter("[", "").substringBefore("]", "N/A")
            } else "N/A"
            
            // Get difficulty
            val difficultyResult = RconManager.sendCommand("difficulty")
            val difficulty = if (difficultyResult.isSuccess) {
                difficultyResult.getOrNull() ?: "Unknown"
            } else "Unknown"
            
            // Update UI
            tvUptime.text = "Seed: $seed"  // Reuse uptime field for seed
            tvOnlinePlayers.text = playerCount.toString()
            tvTps.text = difficulty  // Show difficulty instead of TPS
            tvMemory.text = "Use server plugins for advanced stats"
            
            // Update player list
            if (players.isEmpty()) {
                tvNoPlayers.visibility = View.VISIBLE
                recyclerPlayers.visibility = View.GONE
            } else {
                tvNoPlayers.visibility = View.GONE
                recyclerPlayers.visibility = View.VISIBLE
                playersAdapter.submitList(players)
            }
            
            btnRefresh.isEnabled = true
        }
    }
    
    private fun showMockData() {
        val mockPlayers = listOf("Steve", "Alex", "Notch")
        tvUptime.text = "Not connected"
        tvOnlinePlayers.text = "${mockPlayers.size}"
        tvTps.text = "N/A"
        tvMemory.text = "Connect to see stats"
        
        tvNoPlayers.visibility = View.GONE
        recyclerPlayers.visibility = View.VISIBLE
        playersAdapter.submitList(mockPlayers)
    }
}

// Active Players Adapter
class ActivePlayersAdapter : RecyclerView.Adapter<ActivePlayersAdapter.PlayerViewHolder>() {

    private var players = listOf<String>()

    fun submitList(newPlayers: List<String>) {
        players = newPlayers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(players[position])
    }

    override fun getItemCount() = players.size

    class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val playerName: TextView = view.findViewById(R.id.tv_player_name)

        fun bind(name: String) {
            playerName.text = name
        }
    }
}
