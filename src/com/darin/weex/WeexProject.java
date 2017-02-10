package com.darin.weex;

import com.intellij.openapi.components.ProjectComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darin on 6/17/16.
 */
public class WeexProject implements ProjectComponent {

    @Override
    public void projectOpened() {
        WeexAppConfig.getINSTANCE().init();
    }

    @Override
    public void projectClosed() {
        WeexAppConfig.getINSTANCE().destroy();
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Weex";
    }
}
