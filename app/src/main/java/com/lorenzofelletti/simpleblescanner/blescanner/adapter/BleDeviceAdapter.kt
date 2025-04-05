package com.lorenzofelletti.simpleblescanner.blescanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lorenzofelletti.simpleblescanner.R
import com.lorenzofelletti.simpleblescanner.blescanner.model.BleDevice

/**
 * Adapter for the RecyclerView that shows the found BLE devices.
 */
class BleDeviceAdapter(private val devices: List<BleDevice>) : RecyclerView.Adapter<BleDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceAddressTextView: TextView = itemView.findViewById(R.id.device_address)
        val deviceNameTextView: TextView = itemView.findViewById(R.id.device_name)
        val deviceRssiTextView: TextView = itemView.findViewById(R.id.device_rssi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val deviceView = inflater.inflate(R.layout.device_row_layout, parent, false)
        return ViewHolder(deviceView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceAddressTextView.text = device.address
        holder.deviceNameTextView.text = device.name ?: "Unknown"
        holder.deviceRssiTextView.text = "RSSI: ${device.rssi ?: "N/A"} dBm"
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}