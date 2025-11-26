package com.kilma.raspberrypi.ui.minecraft

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilma.raspberrypi.api.MinecraftRconClient
import kotlinx.coroutines.launch

class MinecraftViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Minecraft Server Control"
    }
    val text: LiveData<String> = _text

    private val _statusText = MutableLiveData<String>().apply {
        value = "Not connected\nEnter server details and RCON password to begin."
    }
    val statusText: LiveData<String> = _statusText

    private val rconClient = MinecraftRconClient()
    private var isConnected = false

    fun connect(ip: String, port: String, password: String) {
        if (ip.isBlank() || port.isBlank() || password.isBlank()) {
            appendToStatus("\n> ERROR: Please fill in all connection fields")
            return
        }

        appendToStatus("\n> Connecting to $ip:$port...")

        rconClient.configure(ip, port.toIntOrNull() ?: 25575, password)

        viewModelScope.launch {
            val result = rconClient.connect()
            if (result.isSuccess) {
                isConnected = true
                appendToStatus("> ${result.getOrNull()}")
                appendToStatus("> Ready to send commands\n")
            } else {
                isConnected = false
                appendToStatus("> ERROR: ${result.exceptionOrNull()?.message}")
                appendToStatus("> Connection failed\n")
            }
        }
    }

    fun setPlayerGameMode(playerName: String, gameMode: String) {
        if (!isConnected) {
            appendToStatus("\n> ERROR: Not connected to server")
            return
        }

        if (playerName.isBlank()) {
            appendToStatus("\n> ERROR: Please enter a player name")
            return
        }

        val command = "gamemode $gameMode $playerName"
        executeCommand(command, "Setting $playerName to $gameMode mode")
    }

    fun giveItem(playerName: String, itemName: String, quantity: String) {
        if (!isConnected) {
            appendToStatus("\n> ERROR: Not connected to server")
            return
        }

        if (playerName.isBlank()) {
            appendToStatus("\n> ERROR: Please enter a player name")
            return
        }

        if (itemName.isBlank()) {
            appendToStatus("\n> ERROR: Please enter an item name")
            return
        }

        val qty = quantity.toIntOrNull() ?: 1
        if (qty < 1 || qty > 64) {
            appendToStatus("\n> ERROR: Quantity must be between 1 and 64")
            return
        }

        val command = "give $playerName $itemName $qty"
        executeCommand(command, "Giving $qty x $itemName to $playerName")
    }

    private fun executeCommand(command: String, description: String) {
        appendToStatus("\n> $description")
        appendToStatus("> Command: /$command")

        viewModelScope.launch {
            val result = rconClient.executeCommand(command)
            if (result.isSuccess) {
                val response = result.getOrNull() ?: "Command executed"
                appendToStatus("> Response: $response\n")
            } else {
                appendToStatus("> ERROR: ${result.exceptionOrNull()?.message}\n")
            }
        }
    }

    private fun appendToStatus(text: String) {
        _statusText.value = _statusText.value + "\n" + text
    }

    override fun onCleared() {
        super.onCleared()
        rconClient.disconnect()
    }
}
