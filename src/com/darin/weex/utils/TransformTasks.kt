package com.darin.weex.utils

import com.darin.weex.WeexAppConfig
import com.darin.weex.datas.WeexElementsParser
import com.darin.weex.datas.WeexSelectText
import com.darin.weex.ui.preview.WeexBrowser
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import javafx.application.Platform

import java.io.File
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by darin on 03/11/2016.
 */
class TransformTasks private constructor() : Disposable {
    private val mUpdatePreviewAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val mLoadUrlAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)

    override fun dispose() {
        mUpdatePreviewAlarm.cancelAllRequests()
        mLoadUrlAlarm.cancelAllRequests()

        mUpdatePreviewAlarm.dispose()
        mLoadUrlAlarm.dispose()
    }

    internal inner class TransformResult(filePath: String) : WeexCmd.CmdExecuteCallback {

        private var filePath: String? = null

        init {
            this.filePath = filePath
        }

        override fun done(processResult: String) {
            loadUrl(filePath!!)
        }
    }

    private val WeexBroserMap = ConcurrentHashMap<String, WeexBrowser>()

    fun updateBrowser(weexFileName: String, browser: WeexBrowser) {
        WeexBroserMap.put(weexFileName, browser)
    }

    fun addTransformTask(weexFile: VirtualFile) {
        updatePreview(weexFile)
    }

    fun addLoadUrlTask(weexFilePath: String) {
        loadUrl(weexFilePath)
    }

    /**
     * parse weex source code and compile all its custom elements and itself

     * @param weexElementsParser the elementsParse that contains the real weex source code and custom elements
     */
    private fun parseTextLocalWithAllModules(weexFilePath: String, weexElementsParser: WeexElementsParser?) {
        /**
         * get custom elements
         */
        if (!WeexSdk.isWeexToolKitReady) {
            val elementsMap = weexElementsParser?.custormElements ?: return
            val errors = elementsMap[WeexElementsParser.ERROR_KEY]
            if (!StringUtil.isEmpty(errors))
                ApplicationManager.getApplication().invokeLater { WeexShow.showPopUp(errors!!) }

            val custormEles = elementsMap.entries.iterator()
            var ele: Map.Entry<String, String>
            val pathPrefix = StringBuilder(WeexAppConfig.TEMP_JS_FILE)

            while (custormEles.hasNext()) {
                ele = custormEles.next()
                val tempFileName = pathPrefix.append(ele.key).append(".js").toString()
                val file = File(tempFileName)
                if (!file.exists())
                    WeexSdk.transform(ele.value, null)
            }
        }

        WeexSdk.transform(weexFilePath, TransformResult(weexFilePath))
    }


    private inner class UpdatePreviewRunnable(internal val weexFile: VirtualFile) : Runnable {
        internal val weexElementsParser: WeexElementsParser?

        init {
            if (WeexSdk.isWeexToolKitReady)
                this.weexElementsParser = null
            else
                this.weexElementsParser = WeexElementsParser(WeexSelectText(weexFile))
        }

        override fun run() {
            parseTextLocalWithAllModules(weexFile.path, weexElementsParser)
        }
    }

    /**
     * update the preview
     */
    private fun updatePreview(weexFile: VirtualFile) {
        mUpdatePreviewAlarm.addRequest(UpdatePreviewRunnable(weexFile), 50L)
    }

    private fun loadUrl(filePath: String) {
        mLoadUrlAlarm.cancelAllRequests()
        if (mLoadUrlAlarm.isDisposed)
            return
        mLoadUrlAlarm.addRequest(Runnable {
            ApplicationManager.getApplication().invokeLater(Runnable {
                runLater(Runnable {
                    val browser = WeexBroserMap[filePath] ?: return@Runnable
                    browser.loadUrl(WeexSdk.getPreviewUrl(filePath, true, null))
                })
            })
        }, 50L)
    }


    private fun runLater(runnable: Runnable) {
        /**
         * jfx thread
         */
        Platform.runLater(runnable)
    }

    companion object {
        var instance = TransformTasks()
    }
}
