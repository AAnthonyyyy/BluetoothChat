package com.hgm.bluetoothchat.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.hgm.bluetoothchat.domain.chat.BluetoothDeviceDomain

/**
 * @author：HGM
 * @created：2023/12/20 0020
 * @description：
 **/
@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain():BluetoothDeviceDomain{
      return BluetoothDeviceDomain(
            name = name,
            macAddress = address
      )
}