package com.kilma.minecraft.ui.servers

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.kilma.minecraft.R
import com.kilma.minecraft.data.SavedServer
import com.kilma.minecraft.rcon.RconManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class ServersFragment : Fragment() {

    private lateinit var viewModel: ServersViewModel
    private lateinit var editServerName: TextInputEditText
    private lateinit var editServerIp: TextInputEditText
    private lateinit var editRconPort: TextInputEditText
    private lateinit var editRconPassword: TextInputEditText
    private lateinit var btnSaveServer: Button
    private lateinit var btnConnect: Button
    private lateinit var btnHelp: ImageButton
    private lateinit var tvConnectionStatus: TextView
    private lateinit var recyclerServers: RecyclerView
    private lateinit var serversAdapter: SavedServersAdapter
    
    private val savedServers = mutableListOf<SavedServer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ServersViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_servers, container, false)
        
        initViews(view)
        loadSavedServers()
        setupRecyclerView()
        setupListeners()
        
        return view
    }
    
    private fun initViews(view: View) {
        editServerName = view.findViewById(R.id.edit_server_name)
        editServerIp = view.findViewById(R.id.edit_server_ip)
        editRconPort = view.findViewById(R.id.edit_rcon_port)
        editRconPassword = view.findViewById(R.id.edit_rcon_password)
        btnSaveServer = view.findViewById(R.id.btn_save_server)
        btnConnect = view.findViewById(R.id.btn_connect)
        btnHelp = view.findViewById(R.id.btn_help)
        tvConnectionStatus = view.findViewById(R.id.tv_connection_status)
        recyclerServers = view.findViewById(R.id.recycler_servers)
    }
    
    private fun setupRecyclerView() {
        recyclerServers.layoutManager = LinearLayoutManager(requireContext())
        serversAdapter = SavedServersAdapter(savedServers, 
            onLoadClick = { server -> loadServer(server) },
            onDeleteClick = { server -> deleteServer(server) }
        )
        recyclerServers.adapter = serversAdapter
    }
    
    private fun setupListeners() {
        btnHelp.setOnClickListener {
            showHelpDialog()
        }
        
        btnSaveServer.setOnClickListener {
            saveServer()
        }
        
        btnConnect.setOnClickListener {
            connectToServer()
        }
    }
    
    private fun showHelpDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("RCON Setup Guide")
            .setMessage("""
                To enable RCON on your Minecraft server:
                
                1. Locate server.properties file:
                   • Windows: minecraft_server_folder/server.properties
                   • Linux: ~/minecraft/server.properties
                   • In server root directory
                
                2. Edit the file and add/modify:
                   enable-rcon=true
                   rcon.port=25575
                   rcon.password=your_password
                
                3. Save the file and restart your Minecraft server
                
                4. In this app:
                   • Enter server IP address
                   • Enter RCON port (default: 25575)
                   • Enter RCON password (from server.properties)
                   • Click Connect
                
                Note: Make sure your firewall allows the RCON port!
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun saveServer() {
        val name = editServerName.text.toString().trim()
        val ip = editServerIp.text.toString().trim()
        val port = editRconPort.text.toString().toIntOrNull() ?: 25575
        val password = editRconPassword.text.toString()
        
        if (name.isEmpty() || ip.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val server = SavedServer(
            id = System.currentTimeMillis(),
            name = name,
            ip = ip,
            port = port,
            password = password
        )
        
        savedServers.add(server)
        serversAdapter.notifyItemInserted(savedServers.size - 1)
        saveToPersistence()
        
        Toast.makeText(requireContext(), "Server saved!", Toast.LENGTH_SHORT).show()
        clearFields()
    }
    
    private fun loadServer(server: SavedServer) {
        editServerName.setText(server.name)
        editServerIp.setText(server.ip)
        editRconPort.setText(server.port.toString())
        editRconPassword.setText(server.password)
        Toast.makeText(requireContext(), "Loaded: ${server.name}", Toast.LENGTH_SHORT).show()
    }
    
    private fun deleteServer(server: SavedServer) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Server")
            .setMessage("Delete ${server.name}?")
            .setPositiveButton("Delete") { _, _ ->
                val position = savedServers.indexOf(server)
                if (position != -1) {
                    savedServers.removeAt(position)
                    serversAdapter.notifyItemRemoved(position)
                    saveToPersistence()
                    Toast.makeText(requireContext(), "Server deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun connectToServer() {
        val ip = editServerIp.text.toString().trim()
        val port = editRconPort.text.toString().toIntOrNull() ?: 25575
        val password = editRconPassword.text.toString()
        
        if (ip.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Enter IP and password", Toast.LENGTH_SHORT).show()
            return
        }
        
        tvConnectionStatus.text = "Connecting to $ip:$port..."
        btnConnect.isEnabled = false
        
        lifecycleScope.launch {
            val connection = RconManager.ServerConnection(ip, port, password)
            val result = RconManager.connect(connection)
            
            if (result.isSuccess) {
                tvConnectionStatus.text = "✓ Connected to $ip:$port"
                tvConnectionStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                Toast.makeText(requireContext(), "Connected successfully!", Toast.LENGTH_SHORT).show()
            } else {
                tvConnectionStatus.text = "✗ Connection failed"
                tvConnectionStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                Toast.makeText(requireContext(), "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
            
            btnConnect.isEnabled = true
        }
    }
    
    private fun clearFields() {
        editServerName.setText("")
        editServerIp.setText("")
        editRconPort.setText("25575")
        editRconPassword.setText("")
    }
    
    private fun loadSavedServers() {
        val prefs = requireContext().getSharedPreferences("servers", Context.MODE_PRIVATE)
        val json = prefs.getString("saved_servers", "[]")
        val type = object : TypeToken<List<SavedServer>>() {}.type
        val servers: List<SavedServer> = Gson().fromJson(json, type) ?: emptyList()
        savedServers.clear()
        savedServers.addAll(servers)
    }
    
    private fun saveToPersistence() {
        val prefs = requireContext().getSharedPreferences("servers", Context.MODE_PRIVATE)
        val json = Gson().toJson(savedServers)
        prefs.edit().putString("saved_servers", json).apply()
    }
}

class SavedServersAdapter(
    private val servers: List<SavedServer>,
    private val onLoadClick: (SavedServer) -> Unit,
    private val onDeleteClick: (SavedServer) -> Unit
) : RecyclerView.Adapter<SavedServersAdapter.ServerViewHolder>() {

    class ServerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serverName: TextView = view.findViewById(R.id.server_name)
        val serverDetails: TextView = view.findViewById(R.id.server_details)
        val btnLoad: Button = view.findViewById(R.id.btn_load_server)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_server)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        holder.serverName.text = server.name
        holder.serverDetails.text = "${server.ip}:${server.port}"
        holder.btnLoad.setOnClickListener { onLoadClick(server) }
        holder.btnDelete.setOnClickListener { onDeleteClick(server) }
    }

    override fun getItemCount() = servers.size
}

