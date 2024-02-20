package march.breeze.locationbluetooth.model

import android.os.ParcelUuid

data class Device(
    val name: String,
    val address: String,
    val uuids: Array<ParcelUuid>
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Device) {
            name == other.name && address == other.address
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }
}