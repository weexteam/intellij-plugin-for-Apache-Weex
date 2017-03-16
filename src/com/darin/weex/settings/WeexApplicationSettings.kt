package com.darin.weex.settings

import com.darin.weex.WeexAppConfig
import com.darin.weex.utils.WeexUtils
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import org.jetbrains.annotations.Nls

import javax.swing.*

/**
 * Created by DongYayun on 2016/5/22.
 */
class WeexApplicationSettings : SearchableConfigurable {

    private var myForm: WeexPreviewSettings? = null

    private val form: WeexPreviewSettings
        get() {
            if (myForm == null) {
                myForm = WeexPreviewSettings()
            }
            return myForm!!
        }


    override fun getId(): String {
        return "Settings.Weex.Preview"
    }

    override fun enableSearch(s: String?): Runnable? {
        return null
    }

    @Nls
    override fun getDisplayName(): String {
        return "WeexPreview"
    }

    override fun getHelpTopic(): String? {
        return null
    }

    override fun createComponent(): JComponent? {
        return form.component
    }

    override fun isModified(): Boolean {
        return form.userSetNpmPath != WeexAppConfig.nodeInstallPath
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        WeexUtils.println("SAVE")
        form.save()
    }

    override fun reset() {
        WeexUtils.println("RESET")
        form.reset()
    }

    override fun disposeUIResources() {

        myForm = null
    }
}
