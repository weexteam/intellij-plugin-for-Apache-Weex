package com.darin.weex.weexToolKit

import com.darin.weex.WeexAppConfig
import com.darin.weex.utils.WeexUtils
import com.intellij.openapi.util.text.StringUtil

import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by darin on 7/14/16.
 */
class WeexToolKit private constructor() {

    var weexProcess: WeexProcess? = null
        private set

    private val executorService = Executors.newCachedThreadPool()

    /**
     * @param filePath               the weex file path
     * *
     * @param startHotReloadCallback callback will be invoked after the weex file has been transformed
     */
    fun syncDoStartWeex(filePath: String, startHotReloadCallback: StartHotReloadCallback?) {
        if (StringUtil.isEmptyOrSpaces(filePath) || startHotReloadCallback == null)
            return
        executorService.submit { doStartWeex(filePath, startHotReloadCallback) }
    }

    /**
     * for syncDoStartWeex

     * @param filePath               the weex file path
     * *
     * @param startHotReloadCallback callback will be invoked after the weex file has been transformed
     */
    private fun doStartWeex(filePath: String, startHotReloadCallback: StartHotReloadCallback) {
        if (weexProcess != null
                && filePath == weexProcess!!.weexFilePath
                && weexProcess!!.process != null) {
            startHotReloadCallback.startOk(weexProcess!!)
        }

        var prePort: Int = 0
        var wsPort: Int = 0

        if (weexProcess != null) {
            weexProcess!!.destory()
            prePort = weexProcess!!.previewServerPort
            wsPort = weexProcess!!.webServicePort
            weexProcess = null
        }

        val processId = Thread.currentThread().id.toInt()

        var process: Process? = null

        if (WeexUtils.isPortHasBeenUsed(prePort.toInt()))
            prePort = generatePreviewPort(processId)

        if (WeexUtils.isPortHasBeenUsed(wsPort.toInt()))
            wsPort = generateWSPort(processId)

        try {
            val cmd = WeexAppConfig.nodeInstallPath + File.separator + "weex --port %d --wsport %d --host %s --qr %s"

            val realCmd = String.format(cmd, prePort, wsPort, WeexAppConfig.getLocalHostIP(false), filePath)

            WeexUtils.println(realCmd)
            process = Runtime.getRuntime().exec(realCmd)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (process == null) {
            WeexUtils.println("doStartWeex and then process is null")
            return
        }


        WeexUtils.println("httpport: " + prePort)
        WeexUtils.println("wsPort: " + wsPort)

        weexProcess = WeexProcess.Builder(process).previewServerPort(prePort.toInt()).webServicePort(wsPort.toInt()).weexFileName(filePath).build()

        startHotReloadCallback.startOk(weexProcess!!)


        try {
            process.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun generatePreviewPort(threadId: Int): Int {
        var port = 8000 + threadId
        while (WeexUtils.isPortHasBeenUsed(port)) {
            port += 50
        }
        return port
    }

    private fun generateWSPort(threadId: Int): Int {
        var port = 9000 + threadId
        while (WeexUtils.isPortHasBeenUsed(port)) {
            port += 50
        }
        return port
    }


    interface StartHotReloadCallback {
        fun startOk(process: WeexProcess)
    }


    fun stopWeexToolKitServer() {
        if (weexProcess != null)
            weexProcess!!.destory()
    }

    companion object {

        val instance = WeexToolKit()
    }

}
