package com.lorenzofelletti.simpleblescanner.blescanner.model

/**
 * A class that represents a BLE device.
 */
data class BleDevice(
    val address: String,       // MAC address
    val name: String? = null,  // Friendly name, nullable
    val rssi: Int? = null      // Signal strength in dBm, nullable
) {
    companion object {
        fun createBleDevicesList(): MutableList<BleDevice> {
            return mutableListOf()
        }
    }
}