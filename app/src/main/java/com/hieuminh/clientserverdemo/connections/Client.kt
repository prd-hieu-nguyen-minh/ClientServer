package com.hieuminh.clientserverdemo.connections

import java.net.Socket

class Client() {
    private var socket: Socket? = null

    suspend fun start(address: String, port: Int): String? {
        return try {
            socket = Socket(address, port)
            null
        } catch (e: Exception) {
            e.message ?: "unknown issue"
        }
    }

    fun stop() {
        socket?.close()
        socket = null
    }

    fun send(message: String) {

    }
}
