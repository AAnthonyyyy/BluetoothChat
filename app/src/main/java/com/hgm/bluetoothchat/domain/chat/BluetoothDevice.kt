package com.hgm.bluetoothchat.domain.chat

/**
 * @author：HGM
 * @created：2023/12/20 0020
 * @description：蓝牙设备模型
 **/
// Android SDK已经存在该名字，使用别名
typealias   BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
      val name: String?,
      val macAddress: String
)
