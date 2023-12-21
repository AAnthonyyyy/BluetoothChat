package com.hgm.bluetoothchat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.hgm.bluetoothchat.domain.chat.BluetoothController
import com.hgm.bluetoothchat.domain.chat.BluetoothDeviceDomain
import com.hgm.bluetoothchat.domain.chat.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

/**
 * @author：HGM
 * @created：2023/12/20 0020
 * @description：蓝牙功能的具体实现
 **/
@SuppressLint("MissingPermission")
class AndroidBluetoothController(
      private val context: Context
) : BluetoothController {

      private val bluetoothManager by lazy {
            context.getSystemService(BluetoothManager::class.java)
      }
      private val bluetoothAdapter by lazy {
            bluetoothManager?.adapter
      }

      private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
      override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
            get() = _scannedDevices.asStateFlow()

      private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
      override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
            get() = _pairedDevices.asStateFlow()

      private val _isConnected = MutableStateFlow(false)
      override val isConnected: StateFlow<Boolean>
            get() = _isConnected.asStateFlow()

      private val _errors = MutableSharedFlow<String>()
      override val errors: SharedFlow<String>
            get() = _errors.asSharedFlow()


      private val foundDeviceReceiver = FoundDeviceReceiver { device ->
            _scannedDevices.update { devices ->
                  val newDevice = device.toBluetoothDeviceDomain()
                  if (newDevice in devices) devices else devices + newDevice
            }
      }

      private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
            if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
                  _isConnected.update { isConnected }
            } else {
                  CoroutineScope(Dispatchers.IO).launch {
                        _errors.tryEmit("无法连接到未配对的设备")
                  }
            }
      }

      private var currentServerSocket: BluetoothServerSocket? = null
      private var currentClientSocket: BluetoothSocket? = null


      init {
            updatePairedDevices()
            context.registerReceiver(
                  bluetoothStateReceiver,
                  IntentFilter().apply {
                        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                  }
            )
      }

      override fun startDiscovery() {
            //if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            //      return
            //}

            context.registerReceiver(
                  foundDeviceReceiver, IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND)
            )

            updatePairedDevices()

            bluetoothAdapter?.startDiscovery()
      }

      override fun stopDiscovery() {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                  return
            }

            bluetoothAdapter?.cancelDiscovery()
      }

      override fun release() {
            context.unregisterReceiver(foundDeviceReceiver)
            context.unregisterReceiver(bluetoothStateReceiver)
            closeConnection()
      }

      private fun updatePairedDevices() {
            //if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            //      return
            //}
            bluetoothAdapter?.bondedDevices?.map { it.toBluetoothDeviceDomain() }?.also { devices ->
                  _pairedDevices.update { devices }
            }
      }

      private fun hasPermission(permission: String): Boolean {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
      }


      override fun startBluetoothServer(): Flow<ConnectionResult> {
            // 以服务端的角度，等待外部客户端的连接
            return flow {
                  //if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                  //      throw SecurityException("没有蓝牙连接权限")
                  //}

                  // 创建蓝牙服务套接字
                  currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                        SERVICE_NAME, UUID.fromString(SERVICE_UUID)
                  )


                  var shouldLoop = true
                  while (shouldLoop) {
                        currentClientSocket = try {
                              // 开始监听，等待客户端的连接
                              currentServerSocket?.accept()
                        } catch (e: IOException) {
                              shouldLoop = false
                              null
                        }

                        emit(ConnectionResult.ConnectionEstablished)

                        currentClientSocket?.let {
                              // 与客户端连接后，关闭监听
                              currentServerSocket?.close()
                        }
                  }
            }.onCompletion {
                  closeConnection()
            }.flowOn(Dispatchers.IO)
      }

      override fun connectToDevices(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
            // 以客户端的角度，通过设备的地址和持有的UUID去连接设备
            return flow {
                  //if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                  //      throw SecurityException("没有蓝牙连接权限")
                  //}

                  val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.macAddress)

                  currentClientSocket = bluetoothDevice
                        ?.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))

                  // 停止扫描
                  stopDiscovery()


                  if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == false) {

                  }

                  currentClientSocket?.let { socket ->
                        try {
                              socket.connect()
                              emit(ConnectionResult.ConnectionEstablished)
                        } catch (e: IOException) {
                              socket.close()
                              currentClientSocket = null
                              emit(ConnectionResult.Error("连接已中断"))
                        }
                  }
            }.onCompletion {
                  closeConnection()
            }.flowOn(Dispatchers.IO)
      }

      override fun closeConnection() {
            currentServerSocket?.close()
            currentClientSocket?.close()
            currentClientSocket = null
            currentServerSocket = null
      }

      companion object {
            const val SERVICE_NAME = "chat_service"
            const val SERVICE_UUID = "90f0089d-371c-45aa-81ed-8311417b5db5"
      }
}