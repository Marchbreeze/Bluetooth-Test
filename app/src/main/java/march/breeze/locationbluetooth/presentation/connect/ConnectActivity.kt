package march.breeze.locationbluetooth.presentation.connect

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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import march.breeze.locationbluetooth.R
import march.breeze.locationbluetooth.databinding.ActivityConnectBinding
import march.breeze.locationbluetooth.model.Device
import march.breeze.locationbluetooth.util.base.BaseActivity
import march.breeze.locationbluetooth.util.extension.getParcelable
import march.breeze.locationbluetooth.util.extension.setOnSingleClickListener
import march.breeze.locationbluetooth.util.extension.toast

class ConnectActivity() : BaseActivity<ActivityConnectBinding>(R.layout.activity_connect) {

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null

    private lateinit var activateResultLauncher: ActivityResultLauncher<Intent>

    private var pairedDeviceList = mutableListOf<Device>()
    private var _pairedListAdapter: PairedListAdapter? = null
    private val pairedListAdapter
        get() = requireNotNull(_pairedListAdapter) { "adapter is not initialized" }

    val searchedDeviceSet = LinkedHashSet<Device>()
    private var _searchListAdapter: SearchListAdapter? = null
    private val searchListAdapter
        get() = requireNotNull(_searchListAdapter) { "adapter is not initialized" }

    private var isPermitted = false

    private var searchReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBluetoothAdapter()
        initStartBtnListener()
        initPairedDeviceBtnListener()
        initDeviceSearchBtnListener()
        initAdapter()
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

    private fun initAdapter() {
        _pairedListAdapter = PairedListAdapter()
        _searchListAdapter = SearchListAdapter()
        binding.rvPairedList.adapter = pairedListAdapter
        binding.rvSearchList.adapter = searchListAdapter
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
                        pairedDevices.forEach { device ->
                            pairedDeviceList.add(Device(device.name, device.address, device.uuids))
                        }
                        pairedListAdapter.submitList(pairedDeviceList)
                        toast("${pairedDeviceList.size}개의 페어링되어 있는 기기를 추가했습니다.")
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
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
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
                    with(binding) {
                        tvSearchListTitle.isVisible = true
                        rvSearchList.isVisible = true
                        progressBarSearch.isVisible = true
                    }
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
                        if (device != null && device.name != null) {
                            searchedDeviceSet.add(
                                Device(
                                    device.name ?: "",
                                    device.address ?: "",
                                    device.uuids ?: arrayOf()
                                )
                            )
                        }
                        searchListAdapter.submitList(searchedDeviceSet.toList())
                        searchListAdapter.notifyDataSetChanged()
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        binding.progressBarSearch.isVisible = false
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
        searchReceiver = null
        _pairedListAdapter = null
        _searchListAdapter = null
    }

    companion object {
        private const val PERMIT_REQUEST = 0
    }
}