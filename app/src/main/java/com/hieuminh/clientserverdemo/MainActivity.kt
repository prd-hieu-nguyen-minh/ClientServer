package com.hieuminh.clientserverdemo

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Runnable
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import java.util.Scanner


class MainActivity : AppCompatActivity() {
    private lateinit var etAddress: EditText
    private lateinit var etPort: EditText
    private lateinit var btConnectToServer: Button
    private lateinit var btStartServer: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvMessage: TextView

    private var serverSocket: ServerSocket? = null
    private var socketClient: Socket? = null
    private val socketClients: MutableList<Socket> = mutableListOf()
    private var os: DataOutputStream? = null
    private lateinit var br: BufferedReader
    private var count: Int = 0
    private var isJoin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etAddress = findViewById(R.id.etAddress)
        etPort = findViewById(R.id.etPort)
        btConnectToServer = findViewById(R.id.btConnectToServer)
        btStartServer = findViewById(R.id.btStartServer)
        tvStatus = findViewById(R.id.tvStatus)
        tvMessage = findViewById(R.id.tvMessage)

        btConnectToServer.setOnClickListener {
            val myTask = MyTask()
            myTask.execute()
        }
        btStartServer.setOnClickListener {
            if (serverSocket != null) {
                val myTask = MyTask()
                myTask.execute()
                return@setOnClickListener
            }
            try {
                serverSocket = ServerSocket(8080)
                btConnectToServer.isEnabled = false
                btStartServer.isEnabled = false
                tvStatus.text = "Start server success"
            } catch (e: Exception) {
                tvStatus.text = "Start server socket error due to: ${e.message}"
                return@setOnClickListener
            }
            Thread(
                Runnable {
                    while (true) try {
                        val staffSocket: Socket? = serverSocket?.accept()
                        if (staffSocket != null) {
                            runOnUiThread {
                                btStartServer.isEnabled = true
                                btStartServer.text = "Send to client"
                                socketClients.add(staffSocket)
                                goToChatPanel(staffSocket, "123")
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        print(e.message)
                        // Do not change this because it spawn try-catch many time while running thread!
                    }
                }
            ).start()
        }
    }

    private fun goToChatPanel(socket: Socket, name: String) {
        tvMessage.text = "$name\n${tvMessage.text}"
        Thread(Runnable {
            while (true) {
                try {
                    val scannerIn = Scanner(socket.getInputStream())
                    while (scannerIn.hasNextLine()) {
                        tvMessage.text = "${scannerIn.nextLine()}\n${tvMessage.text}"
                        Log.d("xxx", tvMessage.text.toString())
                    }
                } catch (e: java.lang.Exception) {
                    Log.d("xxx", e.message.toString())
                    // Do not change this because it spawn try-catch many time while running thread!
                }
            }
        }).start()
        isJoin = true
    }

    inner class MyTask : AsyncTask<String?, Void, String?>() {
        fun connectSever(ip: String, port: String): String? {
            try {
                val socket = Socket(ip, port.toInt()) //ket noi server
                socketClient = socket
                val bf = BufferedReader(InputStreamReader(socket.getInputStream()));
                os = DataOutputStream(socket.getOutputStream())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                return e.message
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                return e.message
            } catch (e: IOException) {
                e.printStackTrace()
                return e.message
            }
            return "connect to server success" //tra ve ket qua từ Server
        }


        override fun onPostExecute(s: String?) {
            if (!isJoin) {
                tvStatus.text = s
                socketClient?.let {
                    goToChatPanel(it, "Client ${(0..10).random()}")
                    btStartServer.isEnabled = false
                }
            }
        }

        override fun doInBackground(vararg p0: String?): String? {
            if (socketClient != null) {
                val out = PrintWriter(socketClient!!.getOutputStream(), true)
                out.println("From client ${(0..10).random()}")
                out.flush()
                return null
            }
            if (socketClients.isNotEmpty()) {
                for (client in socketClients) {
                    val out = PrintWriter(client.getOutputStream(), true)
                    out.println("From Server ${(0..10).random()}")
                    out.flush()
                }
                return null
            }
            return connectSever(etAddress.text.toString(), "8080")
        }
    }
}

