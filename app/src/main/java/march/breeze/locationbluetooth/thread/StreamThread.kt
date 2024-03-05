package march.breeze.locationbluetooth.thread

import android.bluetooth.BluetoothSocket
import android.os.Handler
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MyBluetoothService(
    private val handler: Handler
) {
    private inner class ConnectedThread(
        private val bluetoothSocket: BluetoothSocket
    ) : Thread() {

        private lateinit var inputStream: InputStream
        private lateinit var outputStream: OutputStream
        val buffer = ByteArray(1024)

        init {
            try {
                inputStream = bluetoothSocket.inputStream
                outputStream = bluetoothSocket.outputStream
            } catch (e: IOException) {
                Timber.d(e.message.toString())
            }
        }

        override fun run() {
            // 예외가 발생할 때까지 InputStream 연결
            while (true) {
                // read 에서 반환되는 byte값
                var numBytes: Int = try {
                    inputStream.read(buffer)
                } catch (e: Exception) {
                    Timber.d("기기와의 연결이 끊겼습니다.")
                    break
                }
                // 얻은 byte 값을 UI Activity로 보냄
                val readMsg = handler.obtainMessage(0, numBytes, -1, buffer)
                readMsg.sendToTarget()
            }
        }

        // remote device에 데이터 전송을 위해 액티비티에서 호출
        fun write(bytes: ByteArray) {
            try {
                outputStream.write(bytes)
            } catch (e: IOException) {
                Timber.d(e.message.toString())
            }
        }

        fun cancel() {
            try {
                bluetoothSocket.close()
            } catch (e: IOException) {
                Timber.d(e.message.toString())
            }
        }
    }
}