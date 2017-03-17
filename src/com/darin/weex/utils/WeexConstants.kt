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

    fun hasJavaFx(): Boolean {
        val className = "javafx.embed.swing.JFXPanel"
        var javaFx: Class<*>? = null
        try {
            javaFx = Class.forName(className)
        } catch (e: Exception) {
            return false
        }
        return javaFx != null
    }
}
