package com.darin.weex.ui.actions.newFile

import com.darin.weex.language.WeexIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

/**
 * Created by darin on 8/18/16.
 */
class WeexCreateFileAction : CreateFileFromTemplateAction(WeexCreateFileAction.NEW_WEEX_FILE, "", WeexIcons.ICON), DumbAware {

    override fun buildDialog(project: Project, psiDirectory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(NEW_WEEX_FILE)
                .addKind("Empty file", WeexIcons.ICON, FILE_TEMPLATE)
                .addKind("Simple Application", WeexIcons.ICON, APPLICATION_TEMPLATE)

    }

    override fun getActionName(psiDirectory: PsiDirectory, s: String, s1: String): String {
        return NEW_WEEX_FILE
    }

    companion object {
        val FILE_TEMPLATE = "Weex File"
        val APPLICATION_TEMPLATE = "Weex Application"

        private val NEW_WEEX_FILE = "New Weex File"
    }
}
