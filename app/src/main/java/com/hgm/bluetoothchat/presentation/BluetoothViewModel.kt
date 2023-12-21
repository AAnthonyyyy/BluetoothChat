package com.hgm.bluetoothchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hgm.bluetoothchat.domain.chat.BluetoothController
import com.hgm.bluetoothchat.domain.chat.BluetoothDeviceDomain
import com.hgm.bluetoothchat.domain.chat.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class BluetoothViewModel @Inject constructor(
      private val bluetoothController: BluetoothController
) : ViewModel() {

      private val _state = MutableStateFlow(BluetoothUiState())
      val state = combine(
            bluetoothController.scannedDevices,
            bluetoothController.pairedDevices,
            _state
      ) { scannedDevices, pairedDevices, state ->
            state.copy(
                  scannedDevices = scannedDevices,
                  pairedDevices = pairedDevices
            )
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

      private var deviceConnectionJob: Job? = null


      fun startScan() {
            println("123")
            bluetoothController.startDiscovery()
      }

      fun stopScan() {
            bluetoothController.stopDiscovery()
      }

      init {
            bluetoothController.isConnected.onEach { isConnected ->
                  _state.update { it.copy(isConnected = isConnected) }
            }.launchIn(viewModelScope)

            bluetoothController.errors.onEach { msg ->
                  _state.update { it.copy(errorMessage = msg) }
            }.launchIn(viewModelScope)
      }

      fun waitForIncomingConnections() {
            _state.update { it.copy(isConnecting = true) }
            deviceConnectionJob = bluetoothController
                  .startBluetoothServer()
                  .listen()
      }

      fun connectToDevice(device: BluetoothDeviceDomain) {
            _state.update { it.copy(isConnecting = true) }
            deviceConnectionJob = bluetoothController
                  .connectToDevices(device)
                  .listen()
      }

      fun disconnectFromDevice() {
            deviceConnectionJob?.cancel()
            bluetoothController.closeConnection()
            _state.update {
                  it.copy(
                        isConnecting = false,
                        isConnected = false
                  )
            }
      }


      private fun Flow<ConnectionResult>.listen(): Job {
            return this.onEach { result ->
                  when (result) {
                        is ConnectionResult.ConnectionEstablished -> {
                              _state.update {
                                    it.copy(
                                          isConnected = true,
                                          isConnecting = false,
                                          errorMessage = null
                                    )
                              }
                        }

                        is ConnectionResult.Error -> {
                              _state.update {
                                    it.copy(
                                          isConnected = false,
                                          isConnecting = false,
                                          errorMessage = result.message
                                    )
                              }
                        }
                  }
            }.catch { throwable ->
                  bluetoothController.closeConnection()
                  _state.update {
                        it.copy(
                              isConnected = false,
                              isConnecting = false
                        )
                  }
            }.launchIn(viewModelScope)
      }

      override fun onCleared() {
            super.onCleared()
            bluetoothController.release()
      }
}