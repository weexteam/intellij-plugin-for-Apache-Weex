package com.darin.weex

import com.darin.weex.datas.WeexPrimitiveElements
import com.darin.weex.datas.WeexTemplateCustomEle
import com.intellij.openapi.components.ApplicationComponent

/**
 * Created by DongYayun on 2016/5/14.
 */
class WeexApplication : ApplicationComponent {

    override fun initComponent() {
        WeexPrimitiveElements.initPrivitiveEles()
        WeexTemplateCustomEle.initTemplateString()
    }

    override fun disposeComponent() {
    }

    override fun getComponentName(): String {
        return "WeexApplication"
    }

}
