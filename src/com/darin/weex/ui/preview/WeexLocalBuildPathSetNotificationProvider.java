package com.darin.weex.ui.preview;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.language.WeexFileType;
import com.darin.weex.utils.WeexSdk;
import com.darin.weex.utils.WeexUtils;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by darin on 5/25/16.
 */
public class WeexLocalBuildPathSetNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel> implements DumbAware {
    private static final Key<EditorNotificationPanel> KEY = Key.create("Please set local build path");

    @NotNull
    @Override
    public Key<EditorNotificationPanel> getKey() {
        return KEY;
    }



    @Nullable
    @Override
    public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        if (!WeexFileType.isWeexLanguage(virtualFile)) {
            EditorNotifications.updateAll();
            return null;
        }


        if (WeexAppConfig.getINSTANCE().isNodePathValid(WeexAppConfig.getINSTANCE().getNodeInstallPath())) {
            return null;
        }

        final EditorNotificationPanel panel = new EditorNotificationPanel();
        panel.setText("Please set node env");

        panel.createActionLabel("Please set node env", new Runnable() {
            @Override
            public void run() {
                String choosePath = WeexUtils.chooseNpmPath(panel);
                if (StringUtil.isEmpty(choosePath)) {
                    EditorNotifications.updateAll();
                    return;
                }

                if (WeexAppConfig.getINSTANCE().isNodePathValid(choosePath)) {
                    WeexAppConfig.getINSTANCE().setNodeInstallPath(choosePath);
                    WeexSdk.getIntance().startServe(null);
                }

                EditorNotifications.updateAll();
            }
        });


        return panel;

    }
}
