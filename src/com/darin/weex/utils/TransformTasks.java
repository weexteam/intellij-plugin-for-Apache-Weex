package com.darin.weex.utils;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.datas.WeexElementsParser;
import com.darin.weex.datas.WeexSelectText;
import com.darin.weex.ui.preview.WeexBrowser;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import javafx.application.Platform;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by darin on 03/11/2016.
 */
public class TransformTasks implements Disposable {
    public static TransformTasks instance = new TransformTasks();
    private final Alarm mUpdatePreviewAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
    private final Alarm mLoadUrlAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

    @Override
    public void dispose() {
        mUpdatePreviewAlarm.cancelAllRequests();
        mLoadUrlAlarm.cancelAllRequests();

        mUpdatePreviewAlarm.dispose();
        mLoadUrlAlarm.dispose();
    }

    class TransformResult implements WeexCmd.CmdExecuteCallback {

        private String filePath = null;

        public TransformResult(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void done(String processResult) {
            loadUrl(filePath);
        }
    }

    private ConcurrentHashMap<String, WeexBrowser> WeexBroserMap = new ConcurrentHashMap<String, WeexBrowser>();

    public void updateBrowser(String weexFileName, WeexBrowser browser) {
        WeexBroserMap.put(weexFileName, browser);
    }

    private TransformTasks() {

    }

    public void addTransformTask(VirtualFile weexFile) {
        updatePreview(weexFile);
    }

    public void addLoadUrlTask(String weexFilePath) {
        loadUrl(weexFilePath);
    }

    /**
     * parse weex source code and compile all its custom elements and itself
     *
     * @param weexElementsParser the elementsParse that contains the real weex source code and custom elements
     */
    private void parseTextLocalWithAllModules(final String weexFilePath, WeexElementsParser weexElementsParser) {
        /**
         *   get custom elements
         */
        if (!WeexSdk.getIntance().isWeexToolKitReady()) {
            HashMap<String, String> elementsMap = weexElementsParser.getCustormElements();
            final String errors = elementsMap.get(WeexElementsParser.ERROR_KEY);
            if (!StringUtil.isEmpty(errors))
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        WeexShow.showPopUp(errors);
                    }
                });

            Iterator<Map.Entry<String, String>> custormEles = elementsMap.entrySet().iterator();
            Map.Entry<String, String> ele;
            StringBuilder pathPrefix = new StringBuilder(WeexAppConfig.TEMP_JS_FILE);

            while (custormEles.hasNext()) {
                ele = custormEles.next();
                String tempFileName = pathPrefix.append(ele.getKey()).append(".js").toString();
                File file = new File(tempFileName);
                if (!file.exists())
                    WeexSdk.getIntance().transform(ele.getValue(), null);
            }
        }

        WeexSdk.getIntance().transform(weexFilePath, new TransformResult(weexFilePath));
    }


    private class UpdatePreviewRunnable implements Runnable {
        final WeexElementsParser weexElementsParser;
        final VirtualFile weexFile;

        public UpdatePreviewRunnable(VirtualFile weexFile) {
            this.weexFile = weexFile;
            if (WeexSdk.getIntance().isWeexToolKitReady())
                this.weexElementsParser = null;
            else
                this.weexElementsParser = new WeexElementsParser(new WeexSelectText(weexFile));
        }

        @Override
        public void run() {
            parseTextLocalWithAllModules(weexFile.getPath(), weexElementsParser);
        }
    }

    /**
     * update the preview
     */
    private void updatePreview(VirtualFile weexFile) {
        mUpdatePreviewAlarm.addRequest(new UpdatePreviewRunnable(weexFile), 50L);
    }

    private void loadUrl(final String filePath) {
        mLoadUrlAlarm.cancelAllRequests();
        if (mLoadUrlAlarm.isDisposed())
            return;
        mLoadUrlAlarm.addRequest(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        runLater(new Runnable() {
                            @Override
                            public void run() {
                                WeexBrowser browser = WeexBroserMap.get(filePath);
                                if (browser == null)
                                    return;
                                browser.loadUrl(WeexSdk.getIntance().getPreviewUrl(filePath, true, null));
                            }
                        });
                    }
                });

            }
        }, 50L);
    }


    private void runLater(Runnable runnable) {
        /**
         * 这里是 jfx 线程
         */
        Platform.runLater(runnable);
    }
}
