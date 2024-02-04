package march.breeze.locationbluetooth.connect

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBluetoothStartBtnListener()
        initBluetoothActivateCallback()
        activateBluetooth()
    }

    private fun initBluetoothStartBtnListener() {
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
                if (it.resultCode == RESULT_OK) {
                    toast("블루투스가 활성화되었습니다.")
                } else if (it.resultCode == RESULT_CANCELED) {
                    toast("블루투스 활성화가 취소되었습니다.")
                } else {
                    toast("오류가 발생했습니다.")
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
}