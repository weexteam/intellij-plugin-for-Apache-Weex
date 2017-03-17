package com.darin.weex.utils

import com.darin.weex.WeexAppConfig
import com.darin.weex.WeexAppConfig.LOCAL_IP
import com.darin.weex.utils.WeexCmd.runCmdAsync
import com.darin.weex.utils.WeexCmd.runCmdSync
import com.darin.weex.weexToolKit.WeexToolKit
import com.intellij.openapi.util.text.StringUtil
import java.io.File
import java.util.concurrent.Future

/**
 * Created by darin on 10/9/16.
 */
object WeexSdk {
    private var weexServerThread: Future<*>? = null
    var defaultWeexServerPort = 5678
        private set

    init {
        getCurrentServerWay(false)
    }


    /**
     * Only One way left
     */
    enum class ServerWay constructor(val previewUrl: String, val jsUrl: String, val qrCodePreviewUrl: String, val qrCodeJsUrl: String) {
        WEEX_TOOL_KIT(WEEX_TOOLKIT_PREVIEW_URL_FOR_WEBVIEW, WEEX_TOOLKIT_JS_URL_FOR_WEBVIEW, WEEX_TOOLKIT_PREVIEW_URL_FOR_QRCODE, WEEX_TOOLKIT_JS_URL_FOR_QRCODE)
    }

    /**
     * check the weex installed path is valid or not

     * @param path the select path
     * *
     * @return true the path is valid or invalid
     */
    private fun isWeexToolKitInstalled(path: String): Boolean {
        if (StringUtil.isEmpty(path))
            return false

        val file = File(path)
        return file.exists() && File(file, "weex").exists()
    }

    /**
     * @param reuse whether using pre set status of building method
     * *
     * @return current building method
     */
    fun getCurrentServerWay(reuse: Boolean): ServerWay {
        //Only one way left
        return ServerWay.WEEX_TOOL_KIT
    }

    val isWeexToolKitReady: Boolean
        get() = getCurrentServerWay(true) == ServerWay.WEEX_TOOL_KIT


    /**
     * @param filePath               the weex file's name
     * *
     * @param isWebview              for show or hot reload
     * *
     * @param startHotReloadCallback
     * *
     * @return display this weex file in web view by this url
     */
    fun getPreviewUrl(filePath: String, isWebview: Boolean, startHotReloadCallback: WeexToolKit.StartHotReloadCallback?): String? {
        WeexUtils.println("getPreviewUrl" + filePath)
        val name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".we"))
        when (getCurrentServerWay(false)) {
            WeexSdk.ServerWay.WEEX_TOOL_KIT -> {
                if (isWebview)
                    return String.format(getCurrentServerWay(false).previewUrl, WeexAppConfig.getLocalHostIP(false), defaultWeexServerPort, name)

                //HotReload
                val process = WeexToolKit.instance.weexProcess
                if (process == null || filePath != process.weexFilePath) {

                    WeexToolKit.instance.syncDoStartWeex(filePath, startHotReloadCallback)

                    return null
                }
                return null
            }
            else -> return null
        }
    }

    /**
     * @param filePath               the weex file's name
     * *
     * @param isStatic               whether this url is for previewing
     * *
     * @param startHotReloadCallback startHotReloadCallback will be invoked after start the weex server
     * *
     * @param useHostIp
     * *
     * @return display the weex file's javascript by this url
     */
    fun getJsUrl(filePath: String, isStatic: Boolean, startHotReloadCallback: WeexToolKit.StartHotReloadCallback?, useHostIp: Boolean): String? {

        val name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".we"))
        var ip = LOCAL_IP
        if (useHostIp)
            ip = WeexAppConfig.getLocalHostIP(false)
        when (getCurrentServerWay(false)) {
            WeexSdk.ServerWay.WEEX_TOOL_KIT -> {
                if (isStatic)
                    return String.format(getCurrentServerWay(true).jsUrl, ip, defaultWeexServerPort, name)

                val process = WeexToolKit.instance.weexProcess

                if (process == null || filePath != process.weexFilePath) {
                    WeexToolKit.instance.syncDoStartWeex(filePath, startHotReloadCallback)
                    return null
                }

                return String.format(getCurrentServerWay(true).qrCodeJsUrl, ip, process.previewServerPort, name, process.webServicePort)
            }
            else -> return null
        }
    }


    /**
     * transform the given weex script file to javascript

     * @param weexScript the weex script path
     * *
     * @param callback   callback will be invoked after transform completely
     */
    fun transform(weexScript: String, callback: WeexCmd.CmdExecuteCallback?) {
        val transformCmd: String = addNodePathToCmd(WeexAppConfig.EXE_TRANSFORMER_FILE) + " " + WeexAppConfig.addDoubleQuotationMarks(weexScript) + " -o " + WeexAppConfig.addDoubleQuotationMarks(WeexAppConfig.TEMP_JS_FILE)
//        if (false && WeexSdk.isWeexToolKitReady) {
//            transformCmd = WeexAppConfig.nodeInstallPath + File.separator + "weex " + WeexAppConfig.addDoubleQuotationMarks(weexScript) + " -o " + WeexAppConfig.addDoubleQuotationMarks(WeexAppConfig.TEMP_JS_FILE)
//        } else {
        //        }
        runCmdAsync(transformCmd, callback, null, true)
    }


    /**
     * get a unused port number

     * @return usable port number
     */
    private fun generateWeexServerPort(): Int {
        while (WeexUtils.isPortHasBeenUsed(defaultWeexServerPort)) {
            defaultWeexServerPort++
        }
        return defaultWeexServerPort
    }

    /**
     * start the npm serve with run start shell script.
     * maybe the 'npm run serve' cmd is enough cause we will transform the weex script manually

     * @param callback
     */
    fun startServe(callback: WeexCmd.CmdExecuteCallback?) {
        stopServe()
        val startServerCmd: String = addNodePathToCmd(WeexAppConfig.EXE_HTTP_SERVER_FILE) + " -p " + generateWeexServerPort() + " " + WeexAppConfig.TEMP_JS_FILE
        //        if (false && WeexSdk.getInstance().isWeexToolKitReady()) {
        //            startServerCmd = WeexAppConfig.getINSTANCE().getNodeInstallPath() + File.separator + "weex --port " + generateWeexServerPort() + " --server " + WeexAppConfig.TEMP_JS_FILE;
        //        } else {
        //        }

        WeexUtils.println(startServerCmd)

        weexServerThread = runCmdAsync(startServerCmd, callback, null, true)
    }

    /**
     * kill all the weex serve
     *
     *
     * see resource/shells/stopServe script, we has wrote it into loacal path(PathManager.getConfigPath())
     *
     *
     * then run "sh stopServe"
     *
     *
     *
     *
     * the server will be shutdown after the cmd threadPool shutdown
     */
    fun stopServe() {
        if (WeexSdk.isWeexToolKitReady) {
            WeexToolKit.instance.stopWeexToolKitServer()
        }

        weexServerThread?.cancel(true)
        weexServerThread = null

        WeexCmd.shutdown()
        if (!WeexUtils.isWindows) {
            WeexAppConfig.initStopServeShell(false)

            val stopServeFile = File(WeexAppConfig.DEFAULT_CONFIG_PATH + File.separator + "stopServe")
            runCmdSync("sh " + stopServeFile.path, false, null)
        }

    }


    private val WEEX_TOOLKIT_PREVIEW_URL_FOR_WEBVIEW = "http://%s:%d/index.html?page=%s.js&loader=xhr"
    private val WEEX_TOOLKIT_JS_URL_FOR_WEBVIEW = "http://%s:%d/%s.js"


    private val WEEX_TOOLKIT_JS_URL_FOR_QRCODE = "http://%s:%d/weex_tmp/h5_render/%s.js?wsport=%d"
    private val WEEX_TOOLKIT_PREVIEW_URL_FOR_QRCODE = "http://%s:%d/weex_tmp/h5_render/?hot-reload_controller&page=%s.js&loader=xhr"


    /**
     * /usr/local/bin/node or "C:\\Program Files\\nodejs\\node.exe"

     * @param cmd cmd
     * *
     * @return real cmd string
     */
    private fun addNodePathToCmd(cmd: String): String {

        val nodePath = WeexAppConfig.addDoubleQuotationMarks(WeexAppConfig.nodeInstallPath + File.separator + WeexAppConfig.nodeRealName)

        return nodePath + " " + cmd
    }

}
