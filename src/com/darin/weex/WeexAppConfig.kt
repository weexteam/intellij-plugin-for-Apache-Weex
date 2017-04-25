package com.darin.weex


import com.darin.weex.language.WeexFileChangeAdapter
import com.darin.weex.utils.WeexCmd
import com.darin.weex.utils.WeexCmd.destroyConsoleView
import com.darin.weex.utils.WeexCmd.inputStreamToString
import com.darin.weex.utils.WeexCmd.runCmdSync
import com.darin.weex.utils.WeexConstants
import com.darin.weex.utils.WeexSdk
import com.darin.weex.utils.WeexUtils
import com.darin.weex.utils.WeexUtils.isWindows
import com.darin.weex.utils.WeexUtils.startCheckServerStatus
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.*
import java.net.InetAddress
import java.util.*
import java.util.regex.Pattern

/**
 * Created by darin on 5/23/16.
 */
object WeexAppConfig : Properties() {

    private val weexFileChangeAdapter = WeexFileChangeAdapter()

    init {

        initPaths()

        val file = File(CONFIG_PATH)

        try {
            if (!file.exists())
                if (!file.createNewFile())
                    throw IllegalAccessError("Can't create config file")

            load(FileInputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * initialize the paths of server, jsFile, transformer..
     */
    private fun initPaths() {
        CONFIG_PATH = PathManager.getConfigPath() + File.separator + "options/weex.properties"
        DEFAULT_CONFIG_PATH = PathManager.getConfigPath() + File.separator + "weex-tool"

        TEMP_JS_FILE = File(DEFAULT_CONFIG_PATH, "weex-html5").absolutePath

        http_server_exe_path = addDoubleQuotationMarks(File(DEFAULT_CONFIG_PATH, "serve/bin/serve").absolutePath)

        transformer_exe_path = addDoubleQuotationMarks(File(DEFAULT_CONFIG_PATH, "weex-transformer/bin/transformer.js").absolutePath)
        render_exe_path = TEMP_JS_FILE
        WeexUtils.println(TEMP_JS_FILE)

        WeexConstants.invokeLater(object : Runnable {
            override fun run() {
                val weex_tool = File(DEFAULT_CONFIG_PATH)
                if (!weex_tool.exists())
                    weex_tool.mkdirs()

                initOutputPath()
                /**
                 * un zip render && transformer && server
                 */

                if (transformer_exe_path.isNotEmpty() && !File(transformer_exe_path.replace("\"", "")).exists()) {
                    WeexUtils.unzip(this.javaClass.getResourceAsStream(transformerFilePath), WeexAppConfig.DEFAULT_CONFIG_PATH)
                }

                if (render_exe_path.isNotEmpty() && !File(render_exe_path.replace("\"", ""), "index.html").exists())
                    WeexUtils.unzip(this.javaClass.getResourceAsStream(renderFilePath), WeexAppConfig.DEFAULT_CONFIG_PATH)

                if (http_server_exe_path.isNotEmpty() && !File(http_server_exe_path.replace("\"", "")).exists())
                    WeexUtils.unzip(this.javaClass.getResourceAsStream(serverFilePath), WeexAppConfig.DEFAULT_CONFIG_PATH)

                initStopServeShell(true)

                if (shouldStartServer) {
                    WeexSdk.startServe(null)
                    startCheckServerStatus()
                }
                envReady = true
            }
        })
    }

    private fun setPermission() {
        WeexCmd.runCmdSync("chmod -R 777 " + WeexAppConfig.DEFAULT_CONFIG_PATH + "/*", false, null)
    }

    /**
     * create the default temp script output path
     */
    private fun initOutputPath(): Boolean {
        val weexJsOutputPath = File(WeexAppConfig.TEMP_JS_FILE)
        if (!weexJsOutputPath.exists())
            weexJsOutputPath.mkdirs()
        return true
    }


    /**
     * init server
     */
    internal fun init() {
        VirtualFileManager.getInstance().addVirtualFileListener(weexFileChangeAdapter)

        if (isNodePathValid(nodeInstallPath)) {
            nodeInstallPath = nodeInstallPath
        } else if (isNodePathValid(nodeRealPath)) {
            nodeInstallPath = nodeRealPath
        }

        if (envReady) {
            WeexSdk.startServe(null)
            startCheckServerStatus()
        } else {
            shouldStartServer = true
        }

        try {
            val projects = ProjectManager.getInstance().openProjects
            if (projects.isNotEmpty()) {
                WeexCmd.initConsoleView(projects[0])
            }
        } catch (exception: Exception) {
            destroyConsoleView()
        }

    }


    internal fun destroy() {
        VirtualFileManager.getInstance().removeVirtualFileListener(weexFileChangeAdapter)

        WeexSdk.stopServe()

        /**
         * project has been changed,
         * so we should init the console view again according to the new project
         */
        destroyConsoleView()

        save()
    }

    var splitProportion: Float
        get() = java.lang.Float.valueOf(getProperty(KEY_SPLIT_PROPORTION, "0.5"))
        set(proportion) {
            val proportionString = proportion.toString()
            setProperty(KEY_SPLIT_PROPORTION, proportionString)
        }

    var nodeInstallPath: String
        get() = getProperty(KEY_NOED_INSTALL_PATH, nodeRealPath)
        set(path) = setPropertyAndSave(KEY_NOED_INSTALL_PATH, path)


    var webviewWidth: Int
        get() = Integer.valueOf(getProperty(KEY_WEBVIEW_WIDTH, 669.toString()))
        set(width) = setPropertyAndSave(KEY_WEBVIEW_WIDTH, width.toString())

    var webviewHeight: Int
        get() = Integer.valueOf(getProperty(KEY_WEBVIEW_HEIGHT, 1200.toString()))
        set(height) = setPropertyAndSave(KEY_WEBVIEW_HEIGHT, height.toString())
    var http_server_exe_path: String
        get() = getProperty(KEY_HTTP_SERVER_PATH, "")
        set(value) = setPropertyAndSave(KEY_HTTP_SERVER_PATH, value)
    var transformer_exe_path: String
        get() = getProperty(KEY_TRANSFORMER_PATH, "")
        set(value) = setPropertyAndSave(KEY_TRANSFORMER_PATH, value)
    var render_exe_path: String
        get() = getProperty(KEY_RENDER_PATH, "")
        set(value) = setPropertyAndSave(KEY_RENDER_PATH, value)

    /**
     * @return the Weex installed path that has been set
     */
    val weexGitHubPath: String
        get() = getProperty(KEY_GITHUB_PATH, "")

    /**
     * override setProperty, after set property,save it to local file automatically

     * @param key   property key
     * *
     * @param value property value
     */
    private fun setPropertyAndSave(key: String, value: String) {
        setProperty(key, value)
        save()
    }


    /**
     * check the node installed path is valid or not

     * @param path the select path
     * *
     * @return true the path is valid or invalid
     */
    fun isNodePathValid(path: String): Boolean {
        if (StringUtil.isEmpty(path))
            return false

        val file = File(path)
        if (!file.exists())
            return false

        val files = file.listFiles() ?: return false

        var name: String

        for (f in files) {

            name = f.name

            if (name == NODE_NAME || name == NODE_NAME_WIN) {
                return true
            }
        }

        return false
    }


    private fun save() {
        try {
            store(FileOutputStream(CONFIG_PATH), "config")
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * @param forceUpdate force to get local ip
     * *
     * @return local ip
     */
    fun getLocalHostIP(forceUpdate: Boolean): String {
        val currentTime = System.currentTimeMillis()

        if (forceUpdate || StringUtil.isEmpty(mCurrentIp)) {
            localIp
            lastTime = System.currentTimeMillis()
        } else {
            //ip 缓存时间
            val cacheTime = (60 * 1000).toLong()
            val needUpdate = currentTime - lastTime > cacheTime
            if (needUpdate) {
                localIp
                lastTime = System.currentTimeMillis()
            }
        }
        return mCurrentIp!!
    }

    private val localIp: String?
        get() {
            mCurrentIp = localHostIpFromCmd
            if (StringUtil.isEmpty(mCurrentIp))
                mCurrentIp = localHostIpFromJava
            return mCurrentIp
        }

    private val localHostIpFromJava: String
        get() {
            try {
                return InetAddress.getLocalHost().hostAddress
            } catch (ex: Exception) {
                return LOCAL_IP
            }

        }

    /**
     * get local host ip through cmd

     * @return the ip
     */
    private // except 127.0.0.1
    val localHostIpFromCmd: String?
        get() {
            val ifconfig = runCmdSync(WeexConstants.CMD_GET_IP, false, null).trim { it <= ' ' }
            val m = ipPattern.matcher(ifconfig)
            var ip: String?
            try {
                while (m.find()) {
                    ip = m.group()
                    if (!ip!!.contains(LOCAL_IP)) {
                        return ip
                    }
                }
            } catch (e: Exception) {
                return null
            }

            return null
        }


    /**
     * init stop shell script from "/shells/stopServe"

     * @param forceUpdate force rewrite stop server shell script
     * *
     * @return true means rewriting stop server shell script is successfully or not
     */
    fun initStopServeShell(forceUpdate: Boolean): Boolean {
        val path = WeexAppConfig.DEFAULT_CONFIG_PATH
        val stopServeFile = File(path + File.separator + "stopServe")


        if (stopServeFile.exists() && !forceUpdate) {
            return true
        }
        val stopServe = inputStreamToString(WeexUtils::class.java.getResourceAsStream("/shells/stopServe"), true)

        try {
            val f = FileWriter(stopServeFile)
            f.write(stopServe)
            f.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }


    private lateinit var CONFIG_PATH: String

    /**
     * properties configuration keys
     */
    private val KEY_GITHUB_PATH = "build-path"
    private val KEY_NOED_INSTALL_PATH = "node-install-path"
    private val KEY_SPLIT_PROPORTION = "split-proportion"
    private val KEY_TRANSFORMER_PATH = "transformer-path"
    private val KEY_WEBVIEW_WIDTH = "web-width"
    private val KEY_WEBVIEW_HEIGHT = "web-height"
    private val KEY_HTTP_SERVER_PATH = "http-server-path"
    private val KEY_RENDER_PATH = "render-path"


    /**
     * the default node install path
     */
    private val DEFAULT_NODE_PATH = "/usr/local/bin"
    private val DEFAULT_NODE_PATH_WIN = "C:\\Program Files\\nodejs"

    val defaultNodeInstallPath = if (isWindows) DEFAULT_NODE_PATH else DEFAULT_NODE_PATH_WIN

    private val NODE_NAME = "node"
    private val NODE_NAME_WIN = NODE_NAME + ".exe"


    /**
     * for weex tool-kit
     */
    lateinit var DEFAULT_CONFIG_PATH: String

    lateinit var TEMP_JS_FILE: String

    private val renderFilePath = "/render/render.zip"
    private val serverFilePath = "/render/server.zip"
    private val transformerFilePath = "/render/transformer.zip"

    /**
     * debug option switch
     */
    val isDebug = false

    /**
     * @return node in Mac or linux, node.exe in Windows
     */
    val nodeRealName: String
        get() = if (WeexUtils.isWindows) WeexAppConfig.NODE_NAME_WIN else WeexAppConfig.NODE_NAME

    /**
     * @return "/usr/local/bin" or "C:\\Program Files\\nodejs"
     */
    val nodeRealPath: String
        get() = if (WeexUtils.isWindows) WeexAppConfig.DEFAULT_NODE_PATH_WIN else WeexAppConfig.DEFAULT_NODE_PATH

    /**
     * The cmd will not be executed in Windows if one parameter of the cmd has space

     * @param string cmd parameter
     * *
     * @return cmd parameter with Double quotation  such as make C:\\Program Files\\nodejs\\node.exe ->  "C:\\Program Files\\nodejs\\node.exe"
     */
    fun addDoubleQuotationMarks(string: String): String {
        if (WeexUtils.isWindows && !string.startsWith("\""))
            return "\"" + string + "\""
        return string
    }

    /**
     * @param string
     * *
     * @return
     */
    private fun cutUselessMark(string: String): String {
        return string.replace("\\", "")
    }

    private var lastTime: Long = 0
    var LOCAL_IP = "127.0.0.1"
    private var mCurrentIp: String? = null

    private val IPREG = "(?<=inet ).*(?= netmask)"
    private val ipPattern = Pattern.compile(IPREG)

    var envReady = false
    var shouldStartServer = false
}
