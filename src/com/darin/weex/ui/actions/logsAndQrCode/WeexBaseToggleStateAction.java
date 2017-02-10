package com.darin.weex.ui.actions.logsAndQrCode;

import com.darin.weex.utils.WeexQRCodeUtil;
import com.darin.weex.utils.WeexSdk;
import com.darin.weex.utils.WeexUtils;
import com.darin.weex.weexToolKit.WeexProcess;
import com.darin.weex.weexToolKit.WeexToolKit;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.awt.RelativePoint;
import javafx.application.Platform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.darin.weex.utils.WeexCmd.SyncRunCmd;

/**
 * Created by darin on 5/23/16.
 */
public class WeexBaseToggleStateAction extends AnAction {
    private final String urlPrefix = "http://t.cn?_wx_tpl=";
    private boolean isShowLoginfos;

    private boolean isForShoutao = true;

    WeexBaseToggleStateAction(boolean isShowLoginfos, boolean isForShoutao) {
        this.isShowLoginfos = isShowLoginfos;
        this.isForShoutao = isForShoutao;
    }


    private JLabel qrCodeImage;
    private Balloon qrCode;


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {


        if (isShowLoginfos)
            showLogInfos(anActionEvent);
        else showQrcode(anActionEvent);

    }

    /**
     * show build logs
     *
     * @param anActionEvent the click ActionEvent, we can get weex real code form it
     */
    private void showLogInfos(AnActionEvent anActionEvent) {
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    /**
     * show qrcode after click the qrcode actionbar
     *
     * @param anActionEvent the click ActionEvent, we can get the transformed js weburl form it
     */
    private void showQrcode(AnActionEvent anActionEvent) {

        final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(anActionEvent.getDataContext());


        final String[] url = {""};


        RelativePoint relativePoint = new RelativePoint(new Point(20, 20));
        if (qrCode == null || qrCode.isDisposed())
            qrCode = getQrCodePopup(null);
        qrCode.show(relativePoint, Balloon.Position.below);

        if (file == null)
            return;

        if (WeexSdk.getIntance().isWeexToolKitReady()) {
            if (isForShoutao) {
                url[0] = urlPrefix + WeexSdk.getIntance().getJsUrl(file.getPath(), true, null, true);
            } else {
                url[0] = WeexSdk.getIntance().getJsUrl(file.getPath(), false, new WeexToolKit.StartHotReloadCallback() {
                    @Override
                    public void startOk(final WeexProcess process) {

                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                url[0] = WeexSdk.getIntance().getJsUrl(file.getPath(), false, null, true);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        WeexUtils.println(url[0]);
                                        rePainQrCode(url[0]);
                                    }
                                });
                            }
                        });

                    }
                }, true);
            }
        }

        if (url[0] != null) {
            rePainQrCode(url[0]);
        }

    }

    private void rePainQrCode(String url) {
        qrCodeImage.setName(url);
        ImageIcon ic = (ImageIcon) qrCodeImage.getIcon();
        if (ic != null) {
            ic.setImage(WeexQRCodeUtil.Encode_QR_CODE(url));
        } else {
            ic = new ImageIcon();
            ic.setImage(WeexQRCodeUtil.Encode_QR_CODE(url));
            qrCodeImage.setIcon(ic);
        }

        qrCodeImage.repaint();
    }

    private JLabel getQrCodeImage(String url) {

        if (StringUtil.isEmpty(url)) {
            url = "请稍等, 正在刷新二维码, 请再次扫码";
        }
        ImageIcon ic = new ImageIcon();

        ic.setImage(WeexQRCodeUtil.Encode_QR_CODE(url));

        JLabel qrcode = new JLabel(ic);

        qrcode.setName(url);

        return qrcode;
    }

    private Balloon getQrCodePopup(String url) {
        String title = "Scan with playground";
        if (isForShoutao)
            title += "or Taobao app";
        qrCodeImage = getQrCodeImage(url);
        JBPopupFactory factory = JBPopupFactory.getInstance();
        RelativePoint relativePoint;

        return factory.createDialogBalloonBuilder(qrCodeImage, title)
                .setClickHandler(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String url = qrCodeImage.getName();
                        if (!StringUtil.isEmpty(url) && url.startsWith("http"))
                            SyncRunCmd("open " + url, false, null);
                    }
                }, true)
                .setHideOnClickOutside(true)
                .createBalloon();
    }

}
