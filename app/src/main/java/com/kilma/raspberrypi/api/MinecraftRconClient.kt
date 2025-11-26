package com.kilma.raspberrypi.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MinecraftRconClient {
    private var socket: Socket? = null
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null
    private var serverIp = ""
    private var serverPort = 25575
    private var password = ""
    private var requestId = 1
    private var isAuthenticated = false

    // RCON packet types
    private val SERVERDATA_AUTH = 3
    private val SERVERDATA_AUTH_RESPONSE = 2
    private val SERVERDATA_EXECCOMMAND = 2
    private val SERVERDATA_RESPONSE_VALUE = 0

    fun configure(ip: String, port: Int, password: String) {
        this.serverIp = ip
        this.serverPort = port
        this.password = password
    }

    suspend fun connect(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Close existing connection if any
            disconnect()

            // Connect to server
            socket = Socket(serverIp, serverPort)
            socket?.soTimeout = 5000 // 5 second timeout
            
            inputStream = DataInputStream(socket?.getInputStream())
            outputStream = DataOutputStream(socket?.getOutputStream())

            // Authenticate
            sendPacket(SERVERDATA_AUTH, password)
            val authResponse = receivePacket()

            isAuthenticated = authResponse.id != -1

            if (isAuthenticated) {
                Result.success("Connected to Minecraft server at $serverIp:$serverPort")
            } else {
                disconnect()
                Result.failure(Exception("Authentication failed - invalid RCON password"))
            }
        } catch (e: Exception) {
            disconnect()
            Result.failure(e)
        }
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isAuthenticated) {
                return@withContext Result.failure(Exception("Not connected to server"))
            }

            sendPacket(SERVERDATA_EXECCOMMAND, command)
            val response = receivePacket()

            Result.success(response.payload)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            // Ignore
        }
        inputStream = null
        outputStream = null
        socket = null
        isAuthenticated = false
    }

    private fun sendPacket(type: Int, payload: String) {
        val payloadBytes = payload.toByteArray(Charsets.UTF_8)
        val packetSize = 10 + payloadBytes.size // 4 (id) + 4 (type) + payload + 2 (null terminators)

        val buffer = ByteBuffer.allocate(packetSize + 4) // +4 for size field
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.putInt(packetSize)
        buffer.putInt(requestId++)
        buffer.putInt(type)
        buffer.put(payloadBytes)
        buffer.put(0) // Null terminator for payload
        buffer.put(0) // Null terminator for packet

        outputStream?.write(buffer.array())
        outputStream?.flush()
    }

    private fun receivePacket(): RconPacket {
        val sizeBytes = ByteArray(4)
        inputStream?.readFully(sizeBytes)
        
        val size = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).int
        val packetBytes = ByteArray(size)
        inputStream?.readFully(packetBytes)

        val buffer = ByteBuffer.wrap(packetBytes).order(ByteOrder.LITTLE_ENDIAN)
        val id = buffer.int
        val type = buffer.int

        val payloadBytes = ByteArray(size - 10) // size - (id + type + 2 null terminators)
        buffer.get(payloadBytes)

        val payload = String(payloadBytes, Charsets.UTF_8)

        return RconPacket(id, type, payload)
    }

    data class RconPacket(val id: Int, val type: Int, val payload: String)
}
