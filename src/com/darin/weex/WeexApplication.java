package com.darin.weex;

import com.darin.weex.datas.WeexPrimitiveElements;
import com.darin.weex.datas.WeexTemplateCustomEle;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by DongYayun on 2016/5/14.
 */
public class WeexApplication implements ApplicationComponent {
    public WeexApplication() {
    }

    @Override
    public void initComponent() {
        WeexPrimitiveElements.initPrivitiveEles();
        WeexTemplateCustomEle.initTemplateString();
    }

    @Override
    public void disposeComponent() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "WeexApplication";
    }

}
