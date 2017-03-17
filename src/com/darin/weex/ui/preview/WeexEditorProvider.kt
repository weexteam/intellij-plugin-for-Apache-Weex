package com.darin.weex.ui.preview

import com.darin.weex.language.WeexFileType
import com.darin.weex.language.WeexLanguage
import com.darin.weex.utils.WeexConstants
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider
import com.intellij.openapi.project.PossiblyDumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jdom.Element

/**
 * Created by darin on 5/18/16.
 */
class WeexEditorProvider : FileEditorProvider, PossiblyDumbAware {

    override fun accept(project: Project, virtualFile: VirtualFile): Boolean {
        return WeexFileType.isWeexLanguage(virtualFile)
    }

    override fun createEditor(project: Project, virtualFile: VirtualFile): FileEditor = WeexFxPreviewEditor(project, virtualFile)

    override fun disposeEditor(fileEditor: FileEditor) {

    }

    override fun readState(element: Element, project: Project, virtualFile: VirtualFile): FileEditorState {
        return FileEditorState.INSTANCE
    }

    override fun writeState(fileEditorState: FileEditorState, project: Project, element: Element) {

    }

    override fun getEditorTypeId(): String {
        return EDITOR_TYPE_ID
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }

    override fun isDumbAware(): Boolean {
        return true
    }

    companion object {
        val EDITOR_TYPE_ID = WeexLanguage.NAME + "FxPreviewEditor"
    }
}
