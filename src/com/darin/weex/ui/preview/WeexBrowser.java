package com.darin.weex.ui.preview;

import com.darin.weex.utils.TransformTasks;
import com.darin.weex.utils.WeexSdk;
import com.darin.weex.utils.WeexUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Created by darin on 6/3/16.
 */
public class WeexBrowser extends Region {
    private HBox toolBar;

    WebView browser = new WebView();
    WebEngine webEngine = browser.getEngine();

    private int height = 0;
    private int width = 0;


    public String getWeexFilePath() {
        return weexFilePath;
    }

    private String weexFilePath;

    private boolean jsShowd = false;
    private static final String JS = "WeexShow JavaScript";
    private static final String HTML5 = "WeexShow HTML5";

    public WeexBrowser(int width, int height, String weexFilePath) {
        this.width = width;
        this.height = height;
        this.weexFilePath = weexFilePath;

        final Hyperlink hpl = new Hyperlink("WeexShow JavaScript");
        hpl.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String title;
                String url;
                if (jsShowd) {
                    title = JS;
                    url = WeexSdk.getIntance().getPreviewUrl(getWeexFilePath(), true, null);
                } else {
                    title = HTML5;
                    url = WeexSdk.getIntance().getJsUrl(getWeexFilePath(), true, null, false);
                    WeexUtils.println("js url == " + url);
                }
                webEngine.load(url);

                hpl.setText(title);
                jsShowd = !jsShowd;
            }
        });

        toolBar = new HBox();
        toolBar.getChildren().add(hpl);

        //add components
        getChildren().add(browser);
        getChildren().add(toolBar);

        TransformTasks.instance.updateBrowser(weexFilePath, this);
    }


    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser, 0, 0, w, h - tbHeight, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0, h - tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    public void loadUrl(String url) {
        WeexUtils.println("Will load url = " + url);
        webEngine.load(url);
    }


    @Override
    protected double computePrefWidth(double height) {
        return width;
    }

    @Override
    protected double computePrefHeight(double width) {
        return height;
    }
}
