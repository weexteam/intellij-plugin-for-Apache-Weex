package com.darin.weex.ui.actions.editorLayout

import com.darin.weex.ui.preview.WeexFxPreviewEditor
import com.darin.weex.utils.WeexConstants
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.project.DumbAware

/**
 * Created by darin on 5/23/16.
 */
open class WeexBaseChangeSplitLayoutAction constructor(private val myLayoutToSet: WeexFxPreviewEditor.SplitEditorLayout) : AnAction(), DumbAware, Toggleable {


    override fun update(e: AnActionEvent?) {
        super.update(e)

        val splitFileEditor = findSplitEditor(e!!)
        e.presentation.isEnabled = splitFileEditor != null && WeexConstants.hasJavaFx()

        if (splitFileEditor != null) {
            e.presentation.putClientProperty(Toggleable.SELECTED_PROPERTY, myLayoutToSet.equals(splitFileEditor.currentEditorLayout))
        }
    }

    override fun actionPerformed(anActionEvent: AnActionEvent?) {
        if (anActionEvent == null)
            return

        anActionEvent.presentation.putClientProperty(Toggleable.SELECTED_PROPERTY, true)
        val edit = findSplitEditor(anActionEvent)
        edit?.rePaintView(myLayoutToSet!!)
    }


    fun findSplitEditor(e: AnActionEvent?): WeexFxPreviewEditor? {
        if (e == null)
            return null

        val editor = e.getData(PlatformDataKeys.FILE_EDITOR)
        if (editor is WeexFxPreviewEditor) {
            return editor
        } else {
            return WeexFxPreviewEditor.PARENT_SPLIT_KEY.get(editor)
        }
    }
}
