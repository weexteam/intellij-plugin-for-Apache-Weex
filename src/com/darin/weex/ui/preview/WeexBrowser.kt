package com.darin.weex.ui.preview

import com.darin.weex.utils.TransformTasks
import com.darin.weex.utils.WeexSdk
import com.darin.weex.utils.WeexUtils
import com.sun.javafx.application.PlatformImpl
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.Hyperlink
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.web.WebView

/**
 * Created by darin on 6/3/16.
 */
class WeexBrowser(width: Int, height: Int, val weexFilePath: String) : Region() {
    private val toolBar: HBox

    internal var browser = WebView()
    internal var webEngine = browser.engine

    private var height = 0
    private var width = 0

    private var jsShowd = false

    init {
        this.width = width
        this.height = height

        val hpl = Hyperlink("WeexShow JavaScript")
        hpl.onAction = EventHandler<ActionEvent> {
            val title: String
            var url: String?
            if (jsShowd) {
                title = JS
                url = WeexSdk.getPreviewUrl(weexFilePath, true, null)
            } else {
                title = HTML5
                url = WeexSdk.getJsUrl(weexFilePath, true, null, false)
                WeexUtils.println("js url == " + url)
            }
            webEngine.load(url)

            hpl.text = title
            jsShowd = !jsShowd
        }

        toolBar = HBox()
        toolBar.children.add(hpl)

        //add components
        children.add(browser)
        children.add(toolBar)

        TransformTasks.instance.updateBrowser(weexFilePath, this)
    }


    override fun layoutChildren() {
        val w = getWidth()
        val h = getHeight()
        val tbHeight = toolBar.prefHeight(w)
        layoutInArea(browser, 0.0, 0.0, w, h - tbHeight, 0.0, HPos.CENTER, VPos.CENTER)
        layoutInArea(toolBar, 0.0, h - tbHeight, w, tbHeight, 0.0, HPos.CENTER, VPos.CENTER)
    }

    fun loadUrl(url: String?) {
        WeexUtils.println("Will load url = " + url)
        PlatformImpl.runLater { webEngine.load(url) }
    }


    override fun computePrefWidth(height: Double): Double {
        return width.toDouble()
    }

    override fun computePrefHeight(width: Double): Double {
        return height.toDouble()
    }

    companion object {
        private val JS = "WeexShow JavaScript"
        private val HTML5 = "WeexShow HTML5"
    }
}
