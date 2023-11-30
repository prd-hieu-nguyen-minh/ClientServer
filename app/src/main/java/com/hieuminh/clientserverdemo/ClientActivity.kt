package com.hieuminh.clientserverdemo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hieuminh.clientserverdemo.ConnectStatus.Companion.connectType
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
        etAddress = findViewById(R.id.etAddress)
        etPort = findViewById(R.id.etPort)
        tvMessage = findViewById(R.id.tvMessage)
        btManualConnectToServer = findViewById(R.id.btManualConnectToServer)

        updateStatus(ConnectStatus.BEGIN)
    }

    private fun addLogMessage(message: Any?) {
        tvMessage.text = "${message}\n${tvMessage.text}"
    }

    private fun updateStatus(connectStatus: ConnectStatus, errorMessage: String? = null) {
        this.connectStatus = connectStatus
        btManualConnectToServer.text =
            if (connectStatus in ConnectStatus.connectType) "Stop connect" else "Manual connect to server"
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
        startServerJob = lifecycleScope.launch(Dispatchers.IO) {
            val msg = client.start(address, port)
            withContext(Dispatchers.Main) {
                if (msg == null) {
                    updateStatus(ConnectStatus.SUCCESS)
                } else {
                    updateStatus(ConnectStatus.FAILURE, msg)
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
