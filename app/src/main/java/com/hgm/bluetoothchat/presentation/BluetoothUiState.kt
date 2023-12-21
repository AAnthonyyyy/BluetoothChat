package com.hgm.bluetoothchat.presentation

import com.hgm.bluetoothchat.domain.chat.BluetoothDeviceDomain


data class BluetoothUiState(
      val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
      val pairedDevices: List<BluetoothDeviceDomain> = emptyList(),
      val isConnected: Boolean = false,
      val isConnecting: Boolean = false,
      val errorMessage: String? = null
)
