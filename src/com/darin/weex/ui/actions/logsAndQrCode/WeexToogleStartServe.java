package com.darin.weex.ui.actions.logsAndQrCode;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.ui.preview.WeexFxPreviewEditor;
import com.darin.weex.utils.WeexSdk;
import com.darin.weex.utils.WeexUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by darin on 5/25/16.
 */
public class WeexToogleStartServe extends AnAction {

    private Icon server_on = IconLoader.getIcon("/icons/actions/server_on.png");
    private Icon server_off = IconLoader.getIcon("/icons/actions/server_off.png");
    private boolean lastStatus = false;


    public WeexToogleStartServe() {

        final Presentation presentation = this.getTemplatePresentation();
        // start a thread to update the icon
        WeexUtils.onLocalServerStatusChangeListener listerner = new WeexUtils.onLocalServerStatusChangeListener() {
            @Override
            public void onLocalServerStatusChange(boolean isOn) {
                if (isOn) {
                    presentation.setIcon(server_on);
                    presentation.setText("The local Server is on, Click to Restart the server");
                } else {
                    presentation.setIcon(server_off);
                    presentation.setText("The local Server is off, Click to Start the server");
                }
            }
        };

        WeexUtils.addServerChangeListener(listerner);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        WeexAppConfig.getINSTANCE().getLocalHostIP(true);

        WeexFxPreviewEditor edit = findSplitEditor(anActionEvent);

        if (edit != null)
            edit.getMyToolbarWrapper().refresh();
        else WeexUtils.println("NULLLLLLLL");

        WeexUtils.println(WeexUtils.isServerOn());

        if (WeexSdk.getIntance().isWeexToolKitReady()) {
            WeexSdk.getIntance().startServe(null);
            return;
        }


        if (edit == null)
            return;

    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    @Nullable
    public WeexFxPreviewEditor findSplitEditor(AnActionEvent e) {
        final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
        if (editor instanceof WeexFxPreviewEditor) {
            return (WeexFxPreviewEditor) editor;
        } else {
            return WeexFxPreviewEditor.PARENT_SPLIT_KEY.get(editor);
        }
    }
}

