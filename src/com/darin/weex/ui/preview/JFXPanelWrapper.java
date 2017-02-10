package com.darin.weex.ui.preview;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

/**
 * Created by darin on 26/12/2016.
 */
public class JFXPanelWrapper extends JFXPanel {
    public JFXPanelWrapper() {
        Platform.setImplicitExit(false);
    }

    /**
     * This override fixes the situation of using multiple JFXPanels
     * with jbtabs/splitters when some of them are not showing.
     * On getMinimumSize there is no layout manager nor peer so
     * the result could be #size() which is incorrect.
     * @return zero size
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }
}
