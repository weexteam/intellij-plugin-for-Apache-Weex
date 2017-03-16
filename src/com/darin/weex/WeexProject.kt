package com.darin.weex

import com.intellij.openapi.components.ProjectComponent

/**
 * Created by darin on 6/17/16.
 */
class WeexProject : ProjectComponent {

    override fun projectOpened() {
        WeexAppConfig.init()
    }

    override fun projectClosed() {
        WeexAppConfig.destroy()
    }

    override fun initComponent() {

    }

    override fun disposeComponent() {

    }

    override fun getComponentName(): String {
        return "Weex"
    }
}
