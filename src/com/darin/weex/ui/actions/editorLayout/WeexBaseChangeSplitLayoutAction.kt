package com.darin.weex.ui.actions.editorLayout

import com.darin.weex.ui.preview.WeexFxPreviewEditor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.project.DumbAware

/**
 * Created by darin on 5/23/16.
 */
open class WeexBaseChangeSplitLayoutAction protected constructor(private val myLayoutToSet: WeexFxPreviewEditor.SplitEditorLayout?) : AnAction(), DumbAware, Toggleable {


    override fun update(e: AnActionEvent?) {
        super.update(e)


        val splitFileEditor = findSplitEditor(e!!)
        e.presentation.isEnabled = splitFileEditor != null

        if (myLayoutToSet != null && splitFileEditor != null) {
            e.presentation.putClientProperty(Toggleable.SELECTED_PROPERTY, splitFileEditor.currentEditorLayout == myLayoutToSet)
        }
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        anActionEvent.presentation.putClientProperty(Toggleable.SELECTED_PROPERTY, true)

        val project = anActionEvent.project

        val edit = findSplitEditor(anActionEvent)
        edit?.rePaintView(myLayoutToSet!!)
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
