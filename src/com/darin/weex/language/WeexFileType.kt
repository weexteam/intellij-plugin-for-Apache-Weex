package com.darin.weex.language

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.TemplateLanguageFileType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.NonNls

import javax.swing.*

/**
 * Created by darin on 5/18/16.
 */
class WeexFileType : LanguageFileType, TemplateLanguageFileType {

    private constructor() : super(HTMLLanguage.INSTANCE) {}

    protected constructor(lang: Language) : super(lang) {}

    override fun getName(): String {
        return "Weex"
    }

    override fun getDescription(): String {
        return "Weex script"
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon? {
        return WeexIcons.ICON
    }

    companion object {
        val INSTANCE: LanguageFileType = WeexFileType()
        @NonNls
        val DEFAULT_EXTENSION = "we"
        @NonNls
        val DOT_DEFAULT_EXTENSION = "." + DEFAULT_EXTENSION


        fun isWeexLanguage(file: VirtualFile?): Boolean {
            if (file == null)
                return false

            val fileType = file.fileType ?: return false

            val isWeexFileType = fileType.defaultExtension == DEFAULT_EXTENSION || fileType.name == DEFAULT_EXTENSION

            val fileExtension = file.extension
            if (StringUtil.isEmpty(fileExtension))
                return false
            val isWeexFile = file.extension == DEFAULT_EXTENSION
            return isWeexFile || isWeexFileType
        }
    }


}
