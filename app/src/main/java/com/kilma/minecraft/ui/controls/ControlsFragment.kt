package com.kilma.minecraft.ui.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kilma.minecraft.R
import com.kilma.minecraft.rcon.RconManager
import kotlinx.coroutines.launch

class ControlsFragment : Fragment() {

    private lateinit var viewModel: ControlsViewModel
    private lateinit var editTpFrom: AutoCompleteTextView
    private lateinit var editTpTo: AutoCompleteTextView
    private lateinit var editGamemodePlayer: AutoCompleteTextView
    
    private val activePlayers = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ControlsViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_controls, container, false)
        
        initViews(view)
        setupPlayerDropdowns()
        setupButtons(view)
        
        return view
    }
    
    private fun initViews(view: View) {
        editTpFrom = view.findViewById(R.id.edit_tp_from)
        editTpTo = view.findViewById(R.id.edit_tp_to)
        editGamemodePlayer = view.findViewById(R.id.edit_gamemode_player)
    }
    
    private fun setupPlayerDropdowns() {
        // Fetch active players from server via RCON
        lifecycleScope.launch {
            if (RconManager.isConnected()) {
                val result = RconManager.sendCommand("list")
                if (result.isSuccess) {
                    val response = result.getOrNull() ?: ""
                    activePlayers.clear()
                    val playersPart = response.substringAfter(":", "").trim()
                    if (playersPart.isNotEmpty()) {
                        activePlayers.addAll(playersPart.split(",").map { it.trim() })
                    }
                }
            } else {
                // Fallback to mock data if not connected
                activePlayers.clear()
                activePlayers.addAll(listOf("Steve", "Alex", "Notch", "Herobrine"))
            }
            
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, activePlayers)
            editTpFrom.setAdapter(adapter)
            editTpTo.setAdapter(adapter)
            editGamemodePlayer.setAdapter(adapter)
        }
    }
    
    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btn_teleport).setOnClickListener {
            teleportPlayer()
        }
        
        view.findViewById<Button>(R.id.btn_survival).setOnClickListener {
            changeGamemode("survival")
        }
        
        view.findViewById<Button>(R.id.btn_creative).setOnClickListener {
            changeGamemode("creative")
        }
        
        view.findViewById<Button>(R.id.btn_adventure).setOnClickListener {
            changeGamemode("adventure")
        }
        
        view.findViewById<Button>(R.id.btn_spectator).setOnClickListener {
            changeGamemode("spectator")
        }
        
        view.findViewById<Button>(R.id.btn_set_day).setOnClickListener {
            executeCommand("time set day")
        }
        
        view.findViewById<Button>(R.id.btn_set_night).setOnClickListener {
            executeCommand("time set night")
        }
        
        view.findViewById<Button>(R.id.btn_clear_weather).setOnClickListener {
            executeCommand("weather clear")
        }
    }
    
    private fun teleportPlayer() {
        val from = editTpFrom.text.toString().trim()
        val to = editTpTo.text.toString().trim()
        
        if (from.isEmpty() || to.isEmpty()) {
            Toast.makeText(requireContext(), "Select both players", Toast.LENGTH_SHORT).show()
            return
        }
        
        val command = "tp $from $to"
        executeCommand(command)
    }
    
    private fun changeGamemode(mode: String) {
        val player = editGamemodePlayer.text.toString().trim()
        
        if (player.isEmpty()) {
            Toast.makeText(requireContext(), "Select a player", Toast.LENGTH_SHORT).show()
            return
        }
        
        val command = "gamemode $mode $player"
        executeCommand(command)
    }
    
    private fun executeCommand(command: String) {
        if (!RconManager.isConnected()) {
            Toast.makeText(requireContext(), "Not connected to server", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val result = RconManager.sendCommand(command)
            
            if (result.isSuccess) {
                val response = result.getOrNull() ?: ""
                Toast.makeText(requireContext(), "✓ $response", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

