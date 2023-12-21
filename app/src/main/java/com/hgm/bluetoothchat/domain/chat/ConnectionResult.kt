package com.hgm.bluetoothchat.domain.chat

/**
 * @author：HGM
 * @created：2023/12/21 0021
 * @description：连接状态的几种结果
 **/
sealed interface ConnectionResult {
      // 连接已建立
      object ConnectionEstablished : ConnectionResult
      // 连接错误
      data class Error(val message:String):ConnectionResult
}