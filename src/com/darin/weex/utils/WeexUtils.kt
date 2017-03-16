package com.darin.weex.utils

import com.darin.weex.WeexAppConfig
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import java.io.*
import java.util.*
import javax.swing.JComponent
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


/**
 * Created by darin on 5/23/16.
 */
object WeexUtils {
    private val listerners = ArrayList<onLocalServerStatusChangeListener>()
    private var lastStatus = false


    /**
     * inner function to save test logs

     * @param msg
     */
    private fun savelocal(msg: String) {
        if (!WeexAppConfig.isDebug)
            return
        try {
            val f = FileWriter(File(WeexAppConfig.DEFAULT_CONFIG_PATH, "debug.txt"), true)
            f.write(msg)
            f.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * unzip zip file

     * @param zip        the zip input stream
     * *
     * @param outputPath outputPath of zip files
     * *
     * @return success or not
     */
    fun unzip(zip: InputStream, outputPath: String): Boolean {
        var isUnzipOk = true
        try {
            val Zin = ZipInputStream(zip)//输入源zip路径
            val Bin = BufferedInputStream(Zin)
            var outputFile: File
            var entry: ZipEntry? = Zin.nextEntry
            while ((entry) != null) {
                outputFile = File(outputPath, entry.name)
                if (entry.isDirectory) {
                    entry = Zin.nextEntry
                    continue
                } else {
                    val parent = File(outputFile.parent)
                    if (!parent.exists())
                        if (!parent.mkdirs()) {
                            return false
                        }
                }


                val out = FileOutputStream(outputFile)
                val Bout = BufferedOutputStream(out)
                var b: Int = Bin.read()
                while ((b) != -1) {
                    Bout.write(b)
                    b = Bin.read()
                }
                Bout.close()
                out.close()
                entry = Zin.nextEntry
            }
            Bin.close()
            Zin.close()
        } catch (e: Exception) {
            e.printStackTrace()
            isUnzipOk = false
        }

        return isUnzipOk
    }

    /**
     * unzip zip file

     * @param zip        the unzip file's path
     * *
     * @param outputPath outputPath of zip files
     * *
     * @return success or not
     */
    fun unzip(zip: String, outputPath: String): Boolean {
        try {
            return unzip(FileInputStream(zip), outputPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * whether the port has been used

     * @param port port number
     * *
     * @return true or not
     */
    fun isPortHasBeenUsed(port: Long): Boolean {
        if (port <= 0)
            return true


        if (WeexUtils.isWindows) {
            val result = WeexCmd.SyncRunCmd("netstat -aon", false, null)
            return result.contains(port.toString())
        } else {
            return !StringUtil.isEmpty(WeexCmd.SyncRunCmd("lsof -i tcp:" + port, false, null))
        }

    }

    /**
     * judge whether the local server is on
     * may has a better way to do this

     * @return true means local server is on
     */
    val isServerOn: Boolean
        get() = !WeexUtils.isWindows && WeexCmd.SyncRunCmd("lsof -i tcp:12580", false, null).contains("TCP *:12580 (LISTEN)")


    /**
     * set the npm installed path

     * @param component the parent container of the choose component
     * *
     * @return the selected npm installed path
     */
    fun chooseNpmPath(component: JComponent): String? {
        val path = choosePath()

        if (StringUtil.isEmpty(path))
            return null

        if (!WeexAppConfig.isNodePathValid(path)) {
            WeexShow.showMessage("the path is invalid")
            return null
        }
        return path
    }


    /**
     * @return
     */
    private fun choosePath(): String {
        var path: String? = null
        val project = ProjectManager.getInstance().defaultProject
        val chooseedFile = FileChooser.chooseFile(FileChooserDescriptor(false, true, false, false, false, false), project, project.baseDir)

        if (chooseedFile != null) {
            path = chooseedFile.path
        }

        println(path)
        return path!!

    }

    /**
     * whether http server is running
     */
    fun startCheckServerStatus() {
        val thread = object : Thread() {
            override fun run() {
                super.run()
                while (!this.isInterrupted) {
                    val currentStatus: Boolean

                    try {

                        Thread.sleep((1000 * 5).toLong())

                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    if (WeexSdk.isWeexToolKitReady) {
                        currentStatus = WeexUtils.isPortHasBeenUsed(WeexSdk.defaultWeexServerPort.toLong())
                    } else {
                        currentStatus = WeexUtils.isServerOn
                    }

                    if (lastStatus == currentStatus) {
                        continue
                    }

                    for (listerner in listerners)
                        listerner.onLocalServerStatusChange(currentStatus)

                    lastStatus = currentStatus
                }
            }
        }


        thread.priority = Thread.MIN_PRIORITY
        /**
         * make it be daemon
         */
        thread.isDaemon = true
        thread.start()
    }

    /**
     * will call [onLocalServerStatusChangeListener.onLocalServerStatusChange] if the http server's status is changed

     * @param listener listener
     */
    fun addServerChangeListener(listener: onLocalServerStatusChangeListener?) {
        if (listener != null)
            listerners.add(listener)
    }

    /**
     * remove the listener

     * @param listener
     */
    fun removeServerChangeListener(listener: onLocalServerStatusChangeListener?) {
        if (listener != null)
            listerners.remove(listener)
    }

    interface onLocalServerStatusChangeListener {
        fun onLocalServerStatusChange(isOn: Boolean)
    }


    /**
     * @return current environment is windows operator system
     */
    val isWindows: Boolean
        get() = SystemInfo.isWindows


    fun println(msg: Any?) {
        if (WeexAppConfig.isDebug && msg != null)
            println(msg)
    }

    fun printStack() {
        val ex = Throwable()
        val stackElements = ex.stackTrace
        println("-----------------------------------")
        if (stackElements != null) {
            for (stackElement in stackElements) {
                if (stackElement.className.contains("weex")) {
                    println(stackElement.className + ":" + stackElement.fileName + ":" + stackElement.lineNumber + ":" + stackElement.methodName)
                }
            }
        }
        println("-----------------------------------")
    }

}
