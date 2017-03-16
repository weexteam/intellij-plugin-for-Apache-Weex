package com.darin.weex.utils

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.impl.ContentImpl
import java.io.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException

/**
 * Created by darin on 10/9/16.
 */
object WeexCmd {
    private var threadPool = Executors.newCachedThreadPool()
    private var mConsoleView: ConsoleView? = null
    private var mWindow: ToolWindow? = null


    interface CmdExecuteCallback {
        fun done(processResult: String)
    }

    fun AsyncRunCmd(cmd: String, callback: CmdExecuteCallback?, workDir: File?, showConsole: Boolean): Future<*> {
        return threadPool.submit {
            val result = SyncRunCmd(cmd, showConsole, workDir)
            callback?.done(result)
        }
    }

    /**
     * @param cmd         the cmd that will be executed
     * *
     * @param showConsole show cmd executed string on console or not
     * *
     * @param workDir
     */
    fun SyncRunCmd(cmd: String, showConsole: Boolean, workDir: File?): String {
        WeexUtils.println(cmd)
        var result = ""
        try {
            val process = Runtime.getRuntime().exec(cmd, null, workDir)
            //process.waitFor();
            result = inputStreamToString(process.inputStream, true)
        } catch (e1: Exception) {
            //e1.printStackTrace();
            //WeexAppConfig.getINSTANCE().init();
        }

        if (showConsole && !StringUtil.isEmpty(result)) {
            printLoginConsole(result)
        }


        WeexUtils.println("Done + " + result)
        return result
    }

    /**
     * @param inputStream the inputstream that will be transformed to String text
     * *
     * @param rn          if rn is true, we will add \n at the end of each line
     * *
     * @return the String text that read from the inputStream
     */
    fun inputStreamToString(inputStream: InputStream, rn: Boolean): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val s = StringBuilder()
        var line: String? = bufferedReader.readLine()
        try {
            while (line != null) {
                s.append(line)
                if (rn)
                    s.append("\n")

                line = bufferedReader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return s.toString()
    }

    fun shutdown() {
        threadPool.shutdownNow()
        threadPool = Executors.newCachedThreadPool()
    }

    /**
     * show console view in the tool window

     * @param msg the message that will be shown in the tool window.
     */
    fun printLoginConsole(msg: String?) {
        /**
         * ConsoleViewContentType.USER_INPUT green

         * ConsoleViewContentType.SYSTEM_OUTPUT blue

         * ConsoleViewContentType.ERROR_OUTPUT red

         * ConsoleViewContentType.NORMAL_OUTPUT black
         */

        if (mConsoleView == null || msg == null)
            return
        val type: ConsoleViewContentType

        if (msg.contains("NOTE")) {
            type = ConsoleViewContentType.USER_INPUT
        } else if (msg.contains("ERROR")) {
            type = ConsoleViewContentType.ERROR_OUTPUT
        } else if (msg.contains("WARNING")) {
            type = ConsoleViewContentType.SYSTEM_OUTPUT
        } else {
            type = ConsoleViewContentType.NORMAL_OUTPUT
        }
        try {
            mConsoleView!!.print(msg, type)
        } catch (e: RejectedExecutionException) {
            mConsoleView = null
        }

    }

    private var mManager: ToolWindowManager? = null

    fun initConsoleView(project: Project?) {
        if (mConsoleView != null || project == null)
            return
        mManager = ToolWindowManager.getInstance(project)

        if (mManager == null)
            return

        val id = "Weex Console"

        val factory = TextConsoleBuilderFactory.getInstance()
        val builder = factory.createBuilder(project)
        mConsoleView = builder.console
        mWindow = mManager!!.getToolWindow(id)

        if (mWindow == null) {
            mWindow = mManager!!.registerToolWindow(id, false, ToolWindowAnchor.BOTTOM)
            mWindow!!.contentManager.addContent(ContentImpl(mConsoleView!!.component, "WeexConsole", true))
        }

        mWindow!!.show { WeexUtils.println("Hello world") }
    }

    fun destroyConsoleView() {
        if (mManager != null) {
            mManager!!.unregisterToolWindow("Weex Console")
            mManager = null
        }
        mWindow = null
        if (mConsoleView != null) {
            mConsoleView!!.dispose()
        }
        mConsoleView = null
    }
}
