package com.darin.weex.utils

import com.intellij.openapi.application.ApplicationManager
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

/**
 * Created by darin on 8/18/16.
 */
object WeexConstants {

    val testpath = "/Users/darin/Downloads/javafx-sdk-overlay"
    var JavaFxClassLoader: URLClassLoader? = null

    init {
        Thread(Runnable {
            initJavaFxClassLoader()
        }).start()
    }

    val CMD_GET_IP = "ifconfig -a"

    fun initJavaFxClassLoader() {

        val list = ArrayList<File>()

        scanFiles(testpath, list)

        val urlArray: Array<URL> = Array(list.size, { i -> list[i].toURI().toURL() })

        JavaFxClassLoader = URLClassLoader(urlArray)
    }

    fun scanFiles(path: String, fileList: ArrayList<File>) {
        val file = File(path)

        if (file.isDirectory) {
            file.listFiles().forEach {
                scanFiles(it.absolutePath, fileList)
            }
        } else {
            if (file.absolutePath.endsWith("jar")) {
                fileList.add(file)
            }
        }

    }

    fun invokeLater(runnable: Runnable) {
        ApplicationManager.getApplication().invokeLater(runnable)
    }

    fun hasJavaFx(): Boolean {
        return hasJavaFxInSystem()// || hasJavaFxInDefaultDownloadPath(testpath)
    }

    fun hasJavaFxInSystem(): Boolean {
        val className = "javafx.embed.swing.JFXPanel"
        var javaFx: Class<*>? = null
        try {
            javaFx = Class.forName(className)
        } catch (e: Exception) {
            return false
        }
        return javaFx != null
    }

    fun hasJavaFxInDefaultDownloadPath(javaFxPath: String): Boolean {
        val file = File(javaFxPath, "jre/lib/ext/jfxrt.jar")

        return file.exists()
    }
}
