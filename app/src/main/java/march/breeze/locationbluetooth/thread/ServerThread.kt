package march.breeze.locationbluetooth.thread

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class ServerThread(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val uuid: UUID
) : Thread() {

    private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(SOCKET_NAME, uuid)
    }

    override fun run() {
        var clientSocket: BluetoothSocket? = null
        while (true) {
            try {
                clientSocket = serverSocket?.accept()
            } catch (e: IOException) {
                Timber.d(e.message.orEmpty())
            }
            clientSocket?.let {
                /* 클라이언트 소켓과 관련된 작업..... */
                serverSocket?.close()
            }
            break
        }
    }

    fun cancel() {
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Timber.d(e.message.orEmpty())
        }
    }

    companion object {
        private const val SOCKET_NAME = "server"
    }
}