package com.hgm.bluetoothchat.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hgm.bluetoothchat.domain.chat.BluetoothDevice


@Composable
fun DeviceScreen(
      state: BluetoothUiState,
      onStartScan: () -> Unit,
      onStopScan: () -> Unit,
      onStartServer: () -> Unit,
      onDeviceClick: (BluetoothDevice) -> Unit
) {
      Column(
            modifier = Modifier
                  .fillMaxSize()
      ) {
            BluetoothDeviceList(
                  pairedDevices = state.pairedDevices,
                  scannedDevices = state.scannedDevices,
                  onClick = onDeviceClick,
                  modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
            )
            Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround
            ) {
                  Button(onClick = onStartScan) {
                        Text(text = "开始扫描")
                  }
                  Button(onClick = onStopScan) {
                        Text(text = "停止扫描")
                  }
                  Button(onClick = onStartServer) {
                        Text(text = "开启服务")
                  }
            }
      }
}

@Composable
fun BluetoothDeviceList(
      pairedDevices: List<BluetoothDevice>,
      scannedDevices: List<BluetoothDevice>,
      onClick: (BluetoothDevice) -> Unit,
      modifier: Modifier = Modifier
) {
      LazyColumn(
            modifier = modifier
      ) {
            item {
                  Text(
                        text = "已配对设别",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                  )
            }
            items(pairedDevices) { device ->
                  Text(
                        text = device.name ?: "未知设备",
                        modifier = Modifier
                              .fillMaxWidth()
                              .clickable { onClick(device) }
                              .padding(16.dp)
                  )
            }

            item {
                  Text(
                        text = "发现设备",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                  )
            }
            items(scannedDevices) { device ->
                  Text(
                        text = device.name ?: "未知设备",
                        modifier = Modifier
                              .fillMaxWidth()
                              .clickable { onClick(device) }
                              .padding(16.dp)
                  )
            }
      }
}