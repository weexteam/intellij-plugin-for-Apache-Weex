package com.darin.weex.utils

import com.darin.weex.ui.preview.WeexBrowser
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
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

    private val WeexBrowserMap = ConcurrentHashMap<String, WeexBrowser>()

    fun updateBrowser(weexFileName: String, browser: WeexBrowser) {
        WeexBrowserMap.put(weexFileName, browser)
    }

    fun addTransformTask(weexFile: VirtualFile) {
        updatePreview(weexFile)
    }

    fun addLoadUrlTask(weexFilePath: String) {
        loadUrl(weexFilePath)
    }

    /**
     * parse weex source code and compile all its custom elements and itself

     */
    private fun parseTextLocalWithAllModules(weexFilePath: String) {
        /**
         * get custom elements
         */

        WeexSdk.transform(weexFilePath, TransformResult(weexFilePath))
    }


    private inner class UpdatePreviewRunnable(internal val weexFile: VirtualFile) : Runnable {


        override fun run() {
            parseTextLocalWithAllModules(weexFile.path)
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
                    val browser = WeexBrowserMap[filePath] ?: return@Runnable
                    browser.loadUrl(WeexSdk.getPreviewUrl(filePath, true, null))
                })
            })
        }, 50L)
    }


    private fun runLater(runnable: Runnable) {
        ApplicationManager.getApplication().invokeLater(runnable)
    }

    companion object {
        var instance = TransformTasks()
    }
}
