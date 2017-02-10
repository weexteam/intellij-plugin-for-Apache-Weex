package com.darin.weex.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.darin.weex.utils.WeexCmd.SyncRunCmd;

/**
 * Created by darin on 5/17/16.
 */
public class WeexShow {

    public static void showPopUp(String msg) {
        showPopUp(msg, null);
    }

    public static void showPopUp(String msg, AnActionEvent e) {
        JBPopupFactory factory = JBPopupFactory.getInstance();
        RelativePoint relativePoint;
        relativePoint =
                e == null ?
                        new RelativePoint(new Point(100, 100)) : factory.guessBestPopupLocation(e.getDataContext());

        factory.createHtmlTextBalloonBuilder(msg, MessageType.INFO, null)
                .setDialogMode(true)
                .setFadeoutTime(Integer.MAX_VALUE)
                .createBalloon()
                .show(relativePoint, Balloon.Position.below);
    }


    public static void showMessage(String msg) {
        Messages.showMessageDialog(
                msg,
                "WeexMessage",
                Messages.getErrorIcon()
        );
    }

    public static void showPopUpDialog(AnActionEvent e, final JComponent dialog, String title) {
        JBPopupFactory factory = JBPopupFactory.getInstance();
        RelativePoint relativePoint;
        relativePoint = e == null ?
                new RelativePoint(new Point(20, 20)) : factory.guessBestPopupLocation(e.getDataContext());
        factory.createDialogBalloonBuilder(dialog, title)
                .setClickHandler(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String url = dialog.getName();
                        if (!StringUtil.isEmpty(url) && url.startsWith("http"))
                            SyncRunCmd("open " + url, false, null);
                    }
                }, true)
                .setHideOnClickOutside(true)
                .createBalloon()
                .show(relativePoint, Balloon.Position.below);
    }
}
