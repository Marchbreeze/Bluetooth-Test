package march.breeze.locationbluetooth.connect

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import march.breeze.locationbluetooth.R
import march.breeze.locationbluetooth.databinding.ActivityConnectBinding
import march.breeze.locationbluetooth.util.base.BaseActivity
import march.breeze.locationbluetooth.util.extension.setOnSingleClickListener
import march.breeze.locationbluetooth.util.extension.toast

class ConnectActivity() : BaseActivity<ActivityConnectBinding>(R.layout.activity_connect) {

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var pairedDeviceName = ""
    private var pairedDeviceMACAddress = ""
    private lateinit var pairedDeviceUUID: ParcelUuid

    private var isPermitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStartBtnListener()
        initBluetoothActivateCallback()
        initPairedDeviceBtnListener()
        checkBluetoothPermission()
    }

    private fun initStartBtnListener() {
        binding.btnStartBluetooth.setOnSingleClickListener {
            initBluetoothAdapter()
        }
    }

    private fun initBluetoothAdapter() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            toast("블루투스를 지원하지 않는 기기입니다.")
        } else {
            activateBluetooth()
        }
    }

    private fun initBluetoothActivateCallback() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    RESULT_OK -> {
                        toast("블루투스가 활성화되었습니다.")
                    }

                    RESULT_CANCELED -> {
                        toast("블루투스 활성화가 취소되었습니다.")
                    }

                    else -> {
                        toast("오류가 발생했습니다.")
                    }
                }
            }
    }

    private fun activateBluetooth() {
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                    activityResultLauncher.launch(this)
                }
            } else {
                toast("이미 블루투스가 활성화되어 있습니다.")
            }
        }
    }

    private fun initPairedDeviceBtnListener() {
        binding.btnShowPairedDevice.setOnSingleClickListener {
            getPairedDevices()
        }
    }

    private fun getPairedDevices() {
        checkBluetoothPermission()
        bluetoothAdapter?.let {
            if (it.isEnabled) {
                val pairedDevices: Set<BluetoothDevice> = it.bondedDevices
                if (pairedDevices.isNotEmpty()) {
                    pairedDevices.forEach { device ->
                        pairedDeviceName = device.name
                        pairedDeviceMACAddress = device.address
                        pairedDeviceUUID = device.uuids[0]
                    }
                } else {
                    toast("페어링된 기기가 없습니다")
                }
            } else {
                toast("블루투스가 비활성화되어 있습니다.")
            }
        }
    }

    private fun checkBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERMIT_REQUEST
            )
        } else {
            isPermitted = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMIT_REQUEST) {
            isPermitted =
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter = null
    }

    companion object {
        private const val PERMIT_REQUEST = 0
    }
}