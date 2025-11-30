package com.kilma.minecraft.rcon

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Singleton manager for RCON connections to Minecraft servers
 * Implements Source RCON Protocol: https://developer.valvesoftware.com/wiki/Source_RCON_Protocol
 */
object RconManager {
    private const val TAG = "RconManager"
    private const val SERVERDATA_AUTH = 3
    private const val SERVERDATA_AUTH_RESPONSE = 2
    private const val SERVERDATA_EXECCOMMAND = 2
    private const val SERVERDATA_RESPONSE_VALUE = 0
    
    private val commandMutex = Mutex() // Prevent concurrent command execution
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null
    private var requestId = 1
    private var isConnected = false
    private var serverType: ServerType = ServerType.UNKNOWN
    
    enum class ServerType {
        UNKNOWN,
        VANILLA,      // Official Minecraft server
        SPIGOT,       // Spigot/Paper/Purpur
        FORGE,        // Forge modded
        FABRIC,       // Fabric modded
        BUNGEECORD,   // Proxy server
        OTHER         // Other implementations
    }
    
    data class ServerConnection(
        val host: String,
        val port: Int,
        val password: String
    )
    
    /**
     * Connect to a Minecraft server via RCON
     */
    suspend fun connect(connection: ServerConnection): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Close existing connection if any
            disconnect()
            
            Log.d(TAG, "Connecting to ${connection.host}:${connection.port}")
            
            socket = Socket(connection.host, connection.port)
            socket?.soTimeout = 10000 // 10 second timeout
            socket?.keepAlive = true  // Keep connection alive
            outputStream = DataOutputStream(socket!!.getOutputStream())
            inputStream = DataInputStream(socket!!.getInputStream())
            
            Log.d(TAG, "Socket connected, sending authentication...")
            
            // Send authentication packet
            val authId = requestId++
            sendPacket(authId, SERVERDATA_AUTH, connection.password)
            
            Log.d(TAG, "Auth packet sent, waiting for responses...")
            
            // Read authentication response
            // Some servers send 2 packets, some send 1 - handle both
            val firstResponse = readPacket()
            Log.d(TAG, "First response: id=${firstResponse.id}, type=${firstResponse.type}")
            
            // Check if this is the auth response (id matches) or empty packet (id doesn't match)
            if (firstResponse.id == authId) {
                // Server sent auth response directly (single packet)
                Log.d(TAG, "Authentication successful (single packet response)")
            } else {
                // Server sent empty packet first, read the actual auth response
                val authResponse = readPacket()
                Log.d(TAG, "Auth response: id=${authResponse.id}, type=${authResponse.type}")
                
                if (authResponse.id != authId) {
                    disconnect()
                    return@withContext Result.failure(Exception("Authentication failed: Invalid password"))
                }
                Log.d(TAG, "Authentication successful (two packet response)")
            }
            
            isConnected = true
            Log.d(TAG, "Successfully connected and authenticated")
            
            // Detect server type for optimized handling
            detectServerType()
            
            Result.success("Connected to ${connection.host}:${connection.port}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to connect: ${e.message}", e)
            disconnect()
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during connection: ${e.message}", e)
            disconnect()
            Result.failure(e)
        }
    }
    
    /**
     * Send a command to the connected server
     * Handles various server response patterns
     * Thread-safe with mutex to prevent concurrent command execution
     */
    suspend fun sendCommand(command: String): Result<String> = commandMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (!isConnected || socket == null) {
                    return@withContext Result.failure(Exception("Not connected to server"))
                }
                
                Log.d(TAG, "Sending command: $command")
                
                val cmdId = requestId++
                sendPacket(cmdId, SERVERDATA_EXECCOMMAND, command)
                
                // Collect all response packets until we get the complete response
                val responseBuilder = StringBuilder()
                var lastPacketId = -1
                var attempts = 0
                val maxAttempts = 3
                
                while (attempts < maxAttempts) {
                    try {
                        socket?.soTimeout = 2000 // Short timeout for multi-packet responses
                        val response = readPacket()
                        
                        if (response.id == cmdId) {
                            responseBuilder.append(response.body)
                            lastPacketId = response.id
                            
                            // Check if response is complete (some servers send multiple packets)
                            if (response.body.isEmpty()) {
                                break // Empty packet signals end
                            }
                        } else if (lastPacketId == cmdId) {
                            // Different ID after our command = end of response
                            break
                        }
                        
                        attempts = 0 // Reset on successful read
                    } catch (e: SocketTimeoutException) {
                        // Timeout waiting for more packets = response is complete
                        if (lastPacketId == cmdId) {
                            break
                        }
                        attempts++
                    }
                }
                
                socket?.soTimeout = 10000 // Restore normal timeout
                
                val finalResponse = responseBuilder.toString()
                Log.d(TAG, "Response: $finalResponse")
                
                Result.success(finalResponse)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send command: ${e.message}", e)
                disconnect()
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error sending command: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Disconnect from the server
     */
    fun disconnect() {
        try {
            outputStream?.close()
            inputStream?.close()
            socket?.close()
            Log.d(TAG, "Disconnected from server")
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect: ${e.message}", e)
        } finally {
            socket = null
            outputStream = null
            inputStream = null
            isConnected = false
        }
    }
    
    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean = isConnected && socket?.isConnected == true
    
    /**
     * Detect server type for optimized handling
     */
    private suspend fun detectServerType() {
        try {
            // Try to get server version
            val versionResult = sendCommand("version")
            if (versionResult.isSuccess) {
                val version = versionResult.getOrNull()?.lowercase() ?: ""
                serverType = when {
                    version.contains("paper") || version.contains("purpur") -> ServerType.SPIGOT
                    version.contains("spigot") -> ServerType.SPIGOT
                    version.contains("forge") -> ServerType.FORGE
                    version.contains("fabric") -> ServerType.FABRIC
                    version.contains("bungeecord") || version.contains("waterfall") -> ServerType.BUNGEECORD
                    version.contains("minecraft") -> ServerType.VANILLA
                    else -> ServerType.OTHER
                }
                Log.d(TAG, "Detected server type: $serverType")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not detect server type: ${e.message}")
            serverType = ServerType.UNKNOWN
        }
    }
    
    /**
     * Get detected server type
     */
    fun getServerType(): ServerType = serverType
    
    private fun sendPacket(id: Int, type: Int, body: String) {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val size = 10 + bodyBytes.size // 4 (ID) + 4 (Type) + body + 2 (null terminators)
        
        val buffer = ByteBuffer.allocate(size + 4).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(size)
        buffer.putInt(id)
        buffer.putInt(type)
        buffer.put(bodyBytes)
        buffer.put(0) // Null terminator for body
        buffer.put(0) // Null terminator for packet
        
        outputStream?.write(buffer.array())
        outputStream?.flush()
    }
    
    private fun readPacket(): RconPacket {
        // Read packet size
        val sizeBytes = ByteArray(4)
        inputStream?.readFully(sizeBytes)
        val size = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).int
        
        // Validate packet size (prevent memory issues)
        if (size < 10 || size > 4096) {
            throw IOException("Invalid packet size: $size")
        }
        
        val packetBytes = ByteArray(size)
        inputStream?.readFully(packetBytes)
        
        val buffer = ByteBuffer.wrap(packetBytes).order(ByteOrder.LITTLE_ENDIAN)
        val id = buffer.int
        val type = buffer.int
        
        val bodyBytes = ByteArray(size - 10) // size - id(4) - type(4) - 2 null terminators
        buffer.get(bodyBytes)
        
        val body = String(bodyBytes, Charsets.UTF_8).trim('\u0000')
        
        return RconPacket(id, type, body)
    }
    
    data class RconPacket(
        val id: Int,
        val type: Int,
        val body: String
    )
}
