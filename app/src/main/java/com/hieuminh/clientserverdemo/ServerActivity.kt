package com.hieuminh.clientserverdemo

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hieuminh.clientserverdemo.extensions.ViewExtensions.addLog
import java.net.ServerSocket
import java.net.Socket


class ServerActivity : AppCompatActivity() {
    private var server: ServerSocket? = null
    private var acceptThread: Thread? = null
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        tvStatus = findViewById(R.id.tvServerStatus)
        tvStatus.movementMethod = ScrollingMovementMethod()
    }

    fun startServer(view: View) {
        if (server?.isClosed == false) {
            server?.close()
            tvStatus.addLog("Server was closed")
            return
        }
        try {
            val server = ServerSocket(8080)
            this.server = server
            tvStatus.addLog("Server start success")
            Thread {
                while (true) try {
                    val staffSocket: Socket? = server.accept()
                    if (staffSocket != null) {
                        runOnUiThread {
                            tvStatus.addLog("Has client connect: ${staffSocket.inetAddress.hostAddress} ${staffSocket.port}")
                        }
                    }
                } catch (e: java.lang.Exception) {
                    runOnUiThread {
                        tvStatus.addLog("Has error when client connect, ${e.message}")
                    }
                }
            }.start()
        } catch (e: Exception) {
            tvStatus.addLog("Start server failed, cause: ${e.message}")
        }
    }
}
