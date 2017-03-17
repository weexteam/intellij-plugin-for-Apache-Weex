package com.darin.weex.weexToolKit

/**
 * Created by darin on 7/14/16.
 */
class WeexProcess private constructor(builder: WeexProcess.Builder) {

    var process: Process? = null
        private set
    val previewServerPort: Int
    val webServicePort: Int

    var weexFilePath: String? = null
        private set

    init {
        process = builder.process
        previewServerPort = builder.previewServerPort
        webServicePort = builder.webServicePort
        weexFilePath = builder.weexFileName
    }


    fun destory(): Boolean {
        if (process == null)
            return true

        process!!.destroy()
        try {
            process!!.exitValue()
        } catch (e: IllegalThreadStateException) {
            return false
        }

        process = null
        weexFilePath = null

        return true
    }

    internal class Builder(val process: Process) {

        var previewServerPort: Int = 8081
            private set
        var webServicePort: Int = 8082
            private set

        var weexFileName: String? = null
            private set

        fun previewServerPort(port: Int): Builder {
            previewServerPort = port
            return this
        }

        fun weexFileName(name: String): Builder {
            weexFileName = name
            return this
        }

        fun webServicePort(port: Int): Builder {
            webServicePort = port
            return this
        }

        fun build(): WeexProcess {
            return WeexProcess(this)
        }


    }
}
