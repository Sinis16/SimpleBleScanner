package com.lorenzofelletti.simpleblescanner

import android.Manifest
import android.bluetooth.BluetoothManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.permissions.PermissionManager
import com.lorenzofelletti.permissions.dispatcher.dsl.*
import com.lorenzofelletti.simpleblescanner.blescanner.BleScanManager
import com.lorenzofelletti.simpleblescanner.blescanner.adapter.BleDeviceAdapter
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleDevice
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleScanCallback

class MainActivity : AppCompatActivity() {
    private lateinit var btnStartScan: Button
    private lateinit var permissionManager: PermissionManager
    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager
    private lateinit var foundDevices: MutableList<BleDevice>

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)
        permissionManager buildRequestResultsDispatcher {
            withRequestCode(BLE_PERMISSION_REQUEST_CODE) {
                checkPermissions(blePermissions)
                showRationaleDialog(getString(R.string.ble_permission_rationale))
                doOnGranted {
                    Log.i(TAG, "Permissions granted, starting scan")
                    bleScanManager.scanBleDevices()
                }
                doOnDenied {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.ble_permissions_denied_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // RecyclerView handling
        val rvFoundDevices = findViewById<View>(R.id.rv_found_devices) as RecyclerView
        foundDevices = BleDevice.createBleDevicesList()
        val adapter = BleDeviceAdapter(foundDevices)
        rvFoundDevices.adapter = adapter
        rvFoundDevices.layoutManager = LinearLayoutManager(this)

        // BleManager creation
        btManager = getSystemService(BluetoothManager::class.java)
        bleScanManager = BleScanManager(btManager, 5000, scanCallback = BleScanCallback({
            val address = it?.device?.address
            if (address.isNullOrBlank()) {
                Log.w(TAG, "ScanResult has no valid address, skipping")
                return@BleScanCallback
            }

            // Check required permissions
            val hasScanPermission = ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.BLUETOOTH_SCAN
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasConnectPermission = ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val name: String?
            val rssi: Int?
            if (hasScanPermission && hasConnectPermission) {
                name = it.device?.name
                rssi = it.rssi
            } else {
                name = null
                rssi = null
                Log.w(TAG, "Missing permissions (SCAN: $hasScanPermission, CONNECT: $hasConnectPermission), using address only for $address")
            }

            // Check if device already exists by address
            val existingDeviceIndex = foundDevices.indexOfFirst { it.address == address }
            if (existingDeviceIndex == -1) {
                // New device, add it
                val device = BleDevice(address, name, rssi)
                Log.d(TAG, "Adding new device: Address=$address, Name=$name, RSSI=$rssi")
                foundDevices.add(device)
                adapter.notifyItemInserted(foundDevices.size - 1)
            } else {
                // Update existing device’s name and RSSI
                val existingDevice = foundDevices[existingDeviceIndex]
                val updatedDevice = existingDevice.copy(name = name, rssi = rssi)
                foundDevices[existingDeviceIndex] = updatedDevice
                Log.d(TAG, "Updated device: Address=$address, Name=$name, RSSI=$rssi")
                adapter.notifyItemChanged(existingDeviceIndex)
            }
        }))

        // Adding the actions the manager must do before and after scanning
        bleScanManager.beforeScanActions.add { btnStartScan.isEnabled = false }
        bleScanManager.beforeScanActions.add {
            foundDevices.size.let {
                foundDevices.clear()
                adapter.notifyItemRangeRemoved(0, it)
            }
        }
        bleScanManager.afterScanActions.add { btnStartScan.isEnabled = true }

        // Adding the onclick listener to the start scan button
        btnStartScan = findViewById(R.id.btn_start_scan)
        btnStartScan.setOnClickListener {
            Log.i(TAG, "${it.javaClass.simpleName}:${it.id} - onClick event")
            permissionManager checkRequestAndDispatch BLE_PERMISSION_REQUEST_CODE
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.dispatchOnRequestPermissionsResult(requestCode, grantResults)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val BLE_PERMISSION_REQUEST_CODE = 1
        @RequiresApi(Build.VERSION_CODES.S)
        private val blePermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }
}