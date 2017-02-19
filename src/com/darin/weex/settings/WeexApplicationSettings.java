package com.darin.weex.settings;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.utils.WeexUtils;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by DongYayun on 2016/5/22.
 */
public class WeexApplicationSettings implements SearchableConfigurable {

    @Nullable
    private WeexPreviewSettings myForm = null;

    private WeexPreviewSettings getForm() {
        if (myForm == null) {
            myForm = new WeexPreviewSettings();
        }
        return myForm;
    }


    @NotNull
    @Override
    public String getId() {
        return "Settings.Weex.Preview";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "WeexPreview";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return getForm().getComponent();
    }

    @Override
    public boolean isModified() {
        return !getForm().getUserSetNpmPath().equals(WeexAppConfig.getINSTANCE().getNodeInstallPath());
    }

    @Override
    public void apply() throws ConfigurationException {
        WeexUtils.println("SAVE");
        getForm().save();
    }

    @Override
    public void reset() {
        WeexUtils.println("RESET");
        getForm().reset();
    }

    @Override
    public void disposeUIResources() {

        myForm = null;
    }
}
