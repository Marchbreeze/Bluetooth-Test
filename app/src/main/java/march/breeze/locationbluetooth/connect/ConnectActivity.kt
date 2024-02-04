package march.breeze.locationbluetooth.connect

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import march.breeze.locationbluetooth.R
import march.breeze.locationbluetooth.databinding.ActivityConnectBinding
import march.breeze.locationbluetooth.util.base.BaseActivity
import march.breeze.locationbluetooth.util.extension.getParcelable
import march.breeze.locationbluetooth.util.extension.setOnSingleClickListener
import march.breeze.locationbluetooth.util.extension.toast
import timber.log.Timber

class ConnectActivity() : BaseActivity<ActivityConnectBinding>(R.layout.activity_connect) {

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null

    private lateinit var activateResultLauncher: ActivityResultLauncher<Intent>

    private var availableDeviceList = mutableListOf<BluetoothDevice>()

    private var pairedDeviceName = ""
    private var pairedDeviceMACAddress = ""
    private lateinit var pairedDeviceUUID: ParcelUuid

    private var isPermitted = false

    private lateinit var searchReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBluetoothAdapter()
        initStartBtnListener()
        initPairedDeviceBtnListener()
        initDeviceSearchBtnListener()
        initBluetoothActivateCallback()
        checkBluetoothPermission()
        initSearchBroadcastReceiver()
    }

    private fun initBluetoothAdapter() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            toast("블루투스를 지원하지 않는 기기입니다.")
        }
    }

    private fun initStartBtnListener() {
        binding.btnStartBluetooth.setOnSingleClickListener {
            activateBluetooth()
        }
    }

    private fun initPairedDeviceBtnListener() {
        binding.btnShowPairedDevice.setOnSingleClickListener {
            getPairedDevices()
        }
    }

    private fun initDeviceSearchBtnListener() {
        binding.btnSearchDevice.setOnSingleClickListener {
            searchDevice()
        }
    }

    private fun initBluetoothActivateCallback() {
        activateResultLauncher =
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
                    activateResultLauncher.launch(this)
                }
            } else {
                toast("이미 블루투스가 활성화되어 있습니다.")
            }
        }
    }

    private fun getPairedDevices() {
        checkBluetoothPermission()
        if (isPermitted) {
            bluetoothAdapter?.let {
                if (it.isEnabled) {
                    val pairedDevices: Set<BluetoothDevice> = it.bondedDevices
                    if (pairedDevices.isNotEmpty()) {
                        availableDeviceList = pairedDevices.toMutableList()
                        toast("${availableDeviceList.size}개의 기기가 등록되어 있습니다.")
                    } else {
                        toast("기존에 등록된 기기가 없습니다")
                    }
                } else {
                    toast("블루투스가 비활성화되어 있습니다.")
                }
            }
        } else {
            toast("앱의 권한을 확인해주세요.")
        }
    }

    private fun checkBluetoothPermission() {
        val allPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN
        )
        if (allPermissions.any {
                ActivityCompat.checkSelfPermission(
                    this, it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, allPermissions, PERMIT_REQUEST)
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
                grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }
    }

    private fun searchDevice() {
        checkBluetoothPermission()
        if (isPermitted) {
            bluetoothAdapter?.let {
                if (it.isEnabled) {
                    if (it.isDiscovering) {
                        it.cancelDiscovery()
                        toast("페어링 가능한 기기를 재탐색합니다.")
                    }
                    it.startDiscovery()
                    toast("페어링 가능한 기기를 탐색합니다.")
                } else {
                    toast("블루투스가 비활성화되어 있습니다.")
                }
            }
        } else {
            toast("앱의 권한을 확인해주세요.")
        }
    }

    private fun initSearchBroadcastReceiver() {
        searchReceiver = object : BroadcastReceiver() {

            override fun onReceive(c: Context?, intent: Intent?) {
                checkBluetoothPermission()
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = intent.getParcelable(
                            BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                        )
                        pairedDeviceName = device?.name ?: return
                        pairedDeviceMACAddress = device.address
                        pairedDeviceUUID = device.uuids[0]
                        bluetoothAdapter?.cancelDiscovery()
                        toast("기기 검색을 완료했습니다.")
                    }
                }
            }
        }
        registerReceiver(searchReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter = null
        unregisterReceiver(searchReceiver)
    }

    companion object {
        private const val PERMIT_REQUEST = 0
    }
}