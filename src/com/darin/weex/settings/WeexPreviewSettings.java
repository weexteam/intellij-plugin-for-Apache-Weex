package com.darin.weex.settings;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.utils.WeexShow;
import com.darin.weex.utils.WeexUtils;
import com.intellij.openapi.util.text.StringUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by darin on 5/25/16.
 */
public class WeexPreviewSettings {
    private JPanel mMainPanel;
    private JTextField mNodeInstallPath;
    private JButton mNodePathSelectButton;
    private JLabel mNodePathJLable;
    private String mNodeInstallPathString = WeexAppConfig.getINSTANCE().getNoedInstallPath();




    JPanel getComponent() {
        return mMainPanel;
    }


    WeexPreviewSettings() {
        mNodePathSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choosePath = WeexUtils.chooseNpmPath(getComponent());
                if (!StringUtil.isEmpty(choosePath)) {
                    mNodeInstallPath.setText(choosePath);
                    mNodeInstallPathString = choosePath;
                    mNodeInstallPath.requestFocus();
                }

            }
        });
    }


    void save() {

        String npmPath = mNodeInstallPath.getText().trim();

        boolean isNpmPathValid = WeexAppConfig.getINSTANCE().isNodePathValid(npmPath);

        if (!isNpmPathValid) {
            StringBuilder message = new StringBuilder("node installed path is invalid \r\n");
            if (WeexAppConfig.getINSTANCE().isNodePathValid(WeexAppConfig.DEFAULT_NODE_PATH)) {
                npmPath = WeexAppConfig.DEFAULT_NODE_PATH;
                mNodeInstallPath.setText(npmPath);
                message.append("we will set the default path " + WeexAppConfig.DEFAULT_NODE_PATH);
            }

            WeexShow.showMessage(message.toString());
        }


        WeexAppConfig.getINSTANCE().setNoedInstallPath(npmPath);

    }

    void reset() {

        mNodeInstallPathString = WeexAppConfig.getINSTANCE().getNoedInstallPath();
        /**
         * server editor
         *
         * set server editor visible according to WeexAppConfig.getINSTANCE().REMOTE_SERVER_OPEN
         */
        /**
         * node installed path
         */
        mNodeInstallPath.setText(mNodeInstallPathString);
    }

    public String getUserSetNpmPath() {
        return mNodeInstallPath.getText().trim();
    }
}
