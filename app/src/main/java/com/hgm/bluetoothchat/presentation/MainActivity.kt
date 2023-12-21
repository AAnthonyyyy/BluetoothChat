package com.hgm.bluetoothchat.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.hgm.bluetoothchat.presentation.ui.theme.BluetoothChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

      private val bluetoothManager by lazy {
            applicationContext.getSystemService(BluetoothManager::class.java)
      }
      private val bluetoothAdapter by lazy {
            bluetoothManager?.adapter
      }

      private val isBluetoothEnabled: Boolean
            get() = bluetoothAdapter?.isEnabled == true

      override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //
            ////在 Android 10 还需要开启 gps
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //      val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            //      if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //            Toast.makeText(this@MainActivity, "请您先开启gps,否则蓝牙不可用", Toast.LENGTH_SHORT).show()
            //      }
            //}


            val enableBluetoothLauncher = registerForActivityResult(
                  ActivityResultContracts.StartActivityForResult()
            ) { /* Not needed */ }

            val permissionLauncher = registerForActivityResult(
                  ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                  val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        perms[Manifest.permission.BLUETOOTH_CONNECT] == true
                  } else true

                  if (canEnableBluetooth && !isBluetoothEnabled) {
                        enableBluetoothLauncher.launch(
                              Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        )
                  }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                  permissionLauncher.launch(
                        arrayOf(
                              Manifest.permission.BLUETOOTH_SCAN,
                              Manifest.permission.BLUETOOTH_CONNECT,
                              Manifest.permission.BLUETOOTH_ADMIN,
                              Manifest.permission.ACCESS_FINE_LOCATION,
                              Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                  )
            }

            setContent {
                  BluetoothChatTheme {
                        val viewModel = hiltViewModel<BluetoothViewModel>()
                        val state by viewModel.state.collectAsState()

                        LaunchedEffect(key1 = state.errorMessage) {
                              state.errorMessage?.let {
                                    Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT)
                                          .show()
                              }
                        }

                        LaunchedEffect(key1 = state.isConnected) {
                              if (state.isConnected) {
                                    Toast.makeText(
                                          applicationContext,
                                          "连接成功",
                                          Toast.LENGTH_SHORT
                                    )
                                          .show()
                              }
                        }

                        Surface(
                              color = MaterialTheme.colorScheme.background
                        ) {
                              when {
                                    state.isConnecting -> {
                                          Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                          ) {
                                                CircularProgressIndicator()
                                          }
                                    }

                                    else -> {
                                          DeviceScreen(
                                                state = state,
                                                onStartScan = viewModel::startScan,
                                                onStopScan = viewModel::stopScan,
                                                onStartServer = viewModel::waitForIncomingConnections,
                                                onDeviceClick = viewModel::connectToDevice
                                          )
                                    }
                              }
                        }
                  }
            }
      }
}