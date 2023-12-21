package com.hgm.bluetoothchat.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * @author：HGM
 * @created：2023/12/20 0020
 * @description：控制器，包含使用蓝牙的各种功能
 **/
interface BluetoothController {

      // 扫描到的设备
      val scannedDevices: StateFlow<List<BluetoothDevice>>

      // 匹配了的设备
      val pairedDevices: StateFlow<List<BluetoothDevice>>

      //连接状态
      val isConnected: StateFlow<Boolean>

      val errors: SharedFlow<String>

      // 开始搜索设备
      fun startDiscovery()

      // 停止搜索设备
      fun stopDiscovery()

      // 开启蓝牙服务
      fun startBluetoothServer(): Flow<ConnectionResult>

      // 连接设备
      fun connectToDevices(device: BluetoothDevice): Flow<ConnectionResult>

      // 断开连接
      fun closeConnection()

      // 释放内容和资源
      fun release()
}