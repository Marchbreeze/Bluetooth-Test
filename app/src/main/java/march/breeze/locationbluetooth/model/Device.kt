package march.breeze.locationbluetooth.model

import android.os.ParcelUuid

data class Device(
    val name: String,
    val address: String,
    val uuids: Array<ParcelUuid>
)