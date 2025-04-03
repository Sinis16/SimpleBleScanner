package com.lorenzofelletti.simpleblescanner.blescanner.model

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

class BleScanCallback(
    private val onScanResultAction: (ScanResult?) -> Unit = {},
    private val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {},
    private val onScanFailedAction: (Int) -> Unit = {}
) : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        Log.d(TAG, "bleScanCallback - onScanResults called")
        onScanResultAction(result)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        Log.d(TAG, "bleScanCallback - onBatchScanResults called")
        onBatchScanResultAction(results)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Log.e(TAG, "bleScanCallback - scan failed with error '$errorCode'")
        onScanFailedAction(errorCode)
    }

    companion object {
        private val TAG = BleScanCallback::class.java.simpleName
    }
}