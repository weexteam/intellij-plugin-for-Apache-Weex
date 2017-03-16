package com.darin.weex.ui.actions.logsAndQrCode

import com.darin.weex.WeexAppConfig
import com.darin.weex.ui.preview.WeexFxPreviewEditor
import com.darin.weex.utils.WeexSdk
import com.darin.weex.utils.WeexUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.util.IconLoader

import javax.swing.*

/**
 * Created by darin on 5/25/16.
 */
class WeexToogleStartServe : AnAction() {

    private val server_on = IconLoader.getIcon("/icons/actions/server_on.png")
    private val server_off = IconLoader.getIcon("/icons/actions/server_off.png")
    private val lastStatus = false

    init {

        val presentation = this.templatePresentation
        // start a thread to update the icon
        val listerner = WeexUtils.onLocalServerStatusChangeListener { isOn ->
            if (isOn) {
                presentation.icon = server_on
                presentation.text = "The local Server is on, Click to Restart the server"
            } else {
                presentation.icon = server_off
                presentation.text = "The local Server is off, Click to Start the server"
            }
        }

        WeexUtils.addServerChangeListener(listerner)
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        WeexAppConfig.getLocalHostIP(true)

        val edit = findSplitEditor(anActionEvent)

        if (edit != null)
            edit.myToolbarWrapper!!.refresh()
        else
            WeexUtils.println("NULLLLLLLL")

        WeexUtils.println(WeexUtils.isServerOn())

        if (WeexSdk.isWeexToolKitReady) {
            WeexSdk.startServe(null)
            return
        }


        if (edit == null)
            return

    }

    override fun update(e: AnActionEvent?) {
        super.update(e)
    }

    fun findSplitEditor(e: AnActionEvent): WeexFxPreviewEditor? {
        val editor = e.getData(PlatformDataKeys.FILE_EDITOR)
        if (editor is WeexFxPreviewEditor) {
            return editor
        } else {
            return WeexFxPreviewEditor.PARENT_SPLIT_KEY.get(editor)
        }
    }
}

