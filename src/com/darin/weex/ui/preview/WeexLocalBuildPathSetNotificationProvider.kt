package com.darin.weex.ui.preview

import com.darin.weex.WeexAppConfig
import com.darin.weex.language.WeexFileType
import com.darin.weex.utils.WeexSdk
import com.darin.weex.utils.WeexUtils
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

/**
 * Created by darin on 5/25/16.
 */
class WeexLocalBuildPathSetNotificationProvider : EditorNotifications.Provider<EditorNotificationPanel>(), DumbAware {

    override fun getKey(): Key<EditorNotificationPanel> {
        return KEY
    }


    override fun createNotificationPanel(virtualFile: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
        if (!WeexFileType.isWeexLanguage(virtualFile)) {
            EditorNotifications.updateAll()
            return null
        }


        if (WeexAppConfig.isNodePathValid(WeexAppConfig.nodeInstallPath)) {
            return null
        }

        val panel = EditorNotificationPanel()
        panel.setText("Please set node env")

        panel.createActionLabel("Please set node env", Runnable {
            val choosePath = WeexUtils.chooseNpmPath(panel)
            if (StringUtil.isEmpty(choosePath)) {
                EditorNotifications.updateAll()
                return@Runnable
            }

            if (WeexAppConfig.isNodePathValid(choosePath)) {
                WeexAppConfig.nodeInstallPath = choosePath
                WeexSdk.startServe(null)
            }

            EditorNotifications.updateAll()
        })


        return panel

    }

    companion object {
        private val KEY = Key.create<EditorNotificationPanel>("Please set local build path")
    }
}
