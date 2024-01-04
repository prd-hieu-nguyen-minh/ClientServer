package com.hieuminh.clientserverdemo

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.hieuminh.clientserverdemo.connections.Client
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientActivity : AppCompatActivity() {
    private lateinit var client: Client
    private lateinit var etAddress: EditText
    private lateinit var etPort: EditText
    private lateinit var tvMessage: TextView
    private lateinit var btManualConnectToServer: Button
    private lateinit var btScanQRCode: Button

    private lateinit var connectStatus: ConnectStatus

    private var startServerJob: Job? = null

    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        val qrSuccess = result as? QRResult.QRSuccess
        val address = qrSuccess?.content?.rawValue ?: "unknown"
        connectToServer(address, 8080)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)
        client = Client()
        client.setReceiveListener(::addLogMessage)
        etAddress = findViewById(R.id.etAddress)
        etPort = findViewById(R.id.etPort)
        tvMessage = findViewById(R.id.tvMessage)
        btManualConnectToServer = findViewById(R.id.btManualConnectToServer)
        btScanQRCode = findViewById(R.id.btScanQRCode)
        tvMessage.movementMethod = ScrollingMovementMethod()

        updateStatus(ConnectStatus.BEGIN)
    }

    private fun addLogMessage(message: Any?) {
        tvMessage.text = "${message}\n\n${tvMessage.text}".trim()
    }

    private fun updateStatus(connectStatus: ConnectStatus, errorMessage: String? = null) {
        this.connectStatus = connectStatus
        btManualConnectToServer.text = if (connectStatus in ConnectStatus.connectType) "Stop connect" else "Manual connect to server"
        btScanQRCode.isVisible = connectStatus !in ConnectStatus.connectType
        val message = when (connectStatus) {
            ConnectStatus.BEGIN -> "Waiting a connection"
            ConnectStatus.WAIT -> "Connecting to server..."
            ConnectStatus.SUCCESS -> "Start server successful"
            ConnectStatus.FAILURE -> "Start server failed due to: $errorMessage"
            ConnectStatus.DISCONNECT -> "Server disconnect"
            ConnectStatus.CANCEL -> "Cancel connect to server"
        }
        addLogMessage(message)
    }

    private fun connectToServer(address: String, port: Int) {
        updateStatus(ConnectStatus.WAIT)
        for (i in 0..1000) {
            val client = Client()
            startServerJob = lifecycleScope.launch(Dispatchers.IO) {
                val msg = client.start(address, port)
                withContext(Dispatchers.Main) {
                    if (msg == null) {
                        updateStatus(ConnectStatus.SUCCESS)
                        client.setupReceive(tvMessage)
                    } else {
                        updateStatus(ConnectStatus.FAILURE, msg)
                    }
                }
            }
        }
    }

    fun manualConnectToServer(view: View) {
        if (connectStatus in ConnectStatus.connectType) {
            client.stop()
            startServerJob?.cancel()
            startServerJob = null
            updateStatus(ConnectStatus.CANCEL)
            return
        }
        val address = etAddress.text.toString()
        val port = etPort.text.toString().toInt()
        connectToServer(address, port)
    }

    fun scanQRCode(view: View) {
        scanQrCodeLauncher.launch(null)
    }
}

enum class ConnectStatus {
    BEGIN,
    WAIT,
    SUCCESS,
    FAILURE,
    DISCONNECT,
    CANCEL;

    companion object {
        val connectType = listOf(WAIT, SUCCESS)
    }
}

fun TextView.appendLogText(message: Any?) {
    text = "$message\n\n$text".trim()
}
