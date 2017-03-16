package com.darin.weex.ui.actions.logsAndQrCode

import com.darin.weex.utils.WeexCmd
import com.darin.weex.utils.WeexQRCodeUtil
import com.darin.weex.utils.WeexSdk
import com.darin.weex.utils.WeexUtils
import com.darin.weex.weexToolKit.WeexProcess
import com.darin.weex.weexToolKit.WeexToolKit
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.awt.RelativePoint
import javafx.application.Platform

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import com.darin.weex.utils.WeexCmd.SyncRunCmd

/**
 * Created by darin on 5/23/16.
 */
open class WeexBaseToggleStateAction internal constructor(private val isShowLoginfos: Boolean, isForShoutao: Boolean) : AnAction() {
    private val urlPrefix = "http://t.cn?_wx_tpl="

    private var isForShoutao = true

    init {
        this.isForShoutao = isForShoutao
    }


    private var qrCodeImage: JLabel? = null
    private var qrCode: Balloon? = null


    override fun actionPerformed(anActionEvent: AnActionEvent) {


        if (isShowLoginfos)
            showLogInfos(anActionEvent)
        else
            showQrcode(anActionEvent)

    }

    /**
     * show build logs

     * @param anActionEvent the click ActionEvent, we can get weex real code form it
     */
    private fun showLogInfos(anActionEvent: AnActionEvent) {
    }

    override fun update(e: AnActionEvent?) {
        super.update(e)
    }

    /**
     * show qrcode after click the qrcode actionbar

     * @param anActionEvent the click ActionEvent, we can get the transformed js weburl form it
     */
    private fun showQrcode(anActionEvent: AnActionEvent) {

        val file = CommonDataKeys.VIRTUAL_FILE.getData(anActionEvent.dataContext)


        val url = arrayOf("")


        val relativePoint = RelativePoint(Point(20, 20))
        if (qrCode == null || qrCode!!.isDisposed)
            qrCode = getQrCodePopup(null)
        qrCode!!.show(relativePoint, Balloon.Position.below)

        if (file == null)
            return

        if (WeexSdk.isWeexToolKitReady) {
            if (isForShoutao) {
                url[0] = urlPrefix + WeexSdk.getJsUrl(file.path, true, null, true)
            } else {
                url[0] = WeexSdk.getJsUrl(file.path, false, WeexToolKit.StartHotReloadCallback {
                    ApplicationManager.getApplication().invokeLater {
                        url[0] = WeexSdk.getJsUrl(file.path, false, null, true)!!
                        Platform.runLater {
                            WeexUtils.println(url[0])
                            rePainQrCode(url[0])
                        }
                    }
                }, true)!!
            }
        }

        if (url[0] != null) {
            rePainQrCode(url[0])
        }

    }

    private fun rePainQrCode(url: String) {
        qrCodeImage!!.name = url
        var ic: ImageIcon? = qrCodeImage!!.icon as ImageIcon
        if (ic != null) {
            ic.image = WeexQRCodeUtil.Encode_QR_CODE(url)
        } else {
            ic = ImageIcon()
            ic.image = WeexQRCodeUtil.Encode_QR_CODE(url)
            qrCodeImage!!.icon = ic
        }

        qrCodeImage!!.repaint()
    }

    private fun getQrCodeImage(url: String): JLabel {
        var url = url

        if (StringUtil.isEmpty(url)) {
            url = "Please reScan this qrCode again"
        }
        val ic = ImageIcon()

        ic.image = WeexQRCodeUtil.Encode_QR_CODE(url)

        val qrcode = JLabel(ic)

        qrcode.name = url

        return qrcode
    }

    private fun getQrCodePopup(url: String?): Balloon {
        var title = "Scan with playground"
        if (isForShoutao)
            title += "or Taobao app"
        qrCodeImage = getQrCodeImage(url!!)
        val factory = JBPopupFactory.getInstance()
        val relativePoint: RelativePoint

        return factory.createDialogBalloonBuilder(qrCodeImage!!, title)
                .setClickHandler({
                    val url = qrCodeImage!!.name
                    if (!StringUtil.isEmpty(url) && url.startsWith("http"))
                        WeexCmd.SyncRunCmd("open " + url, false, null)
                }, true)
                .setHideOnClickOutside(true)
                .createBalloon()
    }

}
