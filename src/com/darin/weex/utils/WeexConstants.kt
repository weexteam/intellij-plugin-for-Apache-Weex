package com.darin.weex.utils

import com.intellij.openapi.application.ApplicationManager

/**
 * Created by darin on 8/18/16.
 */
object WeexConstants {
    val CMD_GET_IP = "ifconfig -a"

    fun invokeLater(runnable: Runnable) {
        ApplicationManager.getApplication().invokeLater(runnable)
    }
}
