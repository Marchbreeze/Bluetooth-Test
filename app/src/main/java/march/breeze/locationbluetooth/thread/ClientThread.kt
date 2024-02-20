package march.breeze.locationbluetooth.thread

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class ClientThread(
    private val bluetoothAdapter: BluetoothAdapter,
    private val uuid: UUID,
    private val device: BluetoothDevice,
) : Thread() {

    private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(uuid)
    }

    override fun run() {
        bluetoothAdapter.cancelDiscovery()
        try {
            socket?.let {
                it.connect()
                /* 클라이언트 소켓과 관련된 작업..... */
                // val connectedThread = StreamThread(bluetoothSocket = it)
                // connectedThread.start()
            }
        } catch (e: IOException) {
            socket?.close()
            Timber.d(e.message.orEmpty())
        }
    }

    fun cancel() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Timber.d(e.message.orEmpty())
        }
    }
}