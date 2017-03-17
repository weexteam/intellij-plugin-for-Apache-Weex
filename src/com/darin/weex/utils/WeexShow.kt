package com.darin.weex.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.awt.RelativePoint

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


/**
 * Created by darin on 5/17/16.
 */
object WeexShow {

    @JvmOverloads fun showPopUp(msg: String, e: AnActionEvent? = null) {
        val factory = JBPopupFactory.getInstance()
        val relativePoint: RelativePoint
        relativePoint = if (e == null)
            RelativePoint(Point(100, 100))
        else
            factory.guessBestPopupLocation(e.dataContext)

        factory.createHtmlTextBalloonBuilder(msg, MessageType.INFO, null)
                .setDialogMode(true)
                .setFadeoutTime(Integer.MAX_VALUE.toLong())
                .createBalloon()
                .show(relativePoint, Balloon.Position.below)
    }


    fun showMessage(msg: String) {
        Messages.showMessageDialog(
                msg,
                "WeexMessage",
                Messages.getErrorIcon()
        )
    }

    fun showPopUpDialog(e: AnActionEvent?, dialog: JComponent, title: String) {
        val factory = JBPopupFactory.getInstance()
        val relativePoint: RelativePoint
        relativePoint = if (e == null)
            RelativePoint(Point(20, 20))
        else
            factory.guessBestPopupLocation(e.dataContext)
        factory.createDialogBalloonBuilder(dialog, title)
                .setClickHandler({
                    val url = dialog.name
                    if (!StringUtil.isEmpty(url) && url.startsWith("http"))
                        WeexCmd.runCmdSync("open " + url, false, null)
                }, true)
                .setHideOnClickOutside(true)
                .createBalloon()
                .show(relativePoint, Balloon.Position.below)
    }
}
