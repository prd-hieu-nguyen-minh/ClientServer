package com.hieuminh.clientserverdemo.connections

import android.util.Log
import android.widget.TextView
import com.hieuminh.clientserverdemo.appendLogText
import java.net.Socket
import java.util.Scanner
import kotlinx.coroutines.Runnable

class Client {
    private var socket: Socket? = null
    private var receiveListener: ((String?) -> Unit)? = null

    fun start(address: String, port: Int): String? {
        return try {
            val socket = Socket(address, port)
            this.socket = socket
            null
        } catch (e: Exception) {
            e.message ?: "unknown issue"
        }
    }

    fun setupReceive(textView: TextView) {
        Thread(
            Runnable {
                while (true) {
                    try {
                        val scannerIn = Scanner(socket!!.getInputStream())
                        while (scannerIn.hasNextLine()) {
                            textView.appendLogText(scannerIn.nextLine())
                        }
                    } catch (e: java.lang.Exception) {
                        Log.d("xxx", e.message.toString())
                        break
                    }
                }
            }
        ).start()
    }

    fun stop() {
        socket?.close()
        socket = null
        receiveListener = null
    }

    fun send(message: String) {

    }

    fun setReceiveListener(listener: ((String?) -> Unit)?) {
        this.receiveListener = listener
    }
}
