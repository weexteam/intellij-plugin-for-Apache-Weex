package com.darin.weex.weexToolKit;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.utils.WeexUtils;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by darin on 7/14/16.
 */
public class WeexToolKit {

    private static WeexToolKit instance = new WeexToolKit();

    public WeexProcess getWeexProcess() {
        return weexProcess;
    }

    private WeexProcess weexProcess;

    public static WeexToolKit getInstance() {
        return instance;
    }

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private WeexToolKit() {

    }

    /**
     * @param filePath               the weex file path
     * @param startHotReloadCallback callback will be invoked after the weex file has been transformed
     */
    public void syncDoStartWeex(final String filePath, final StartHotReloadCallback startHotReloadCallback) {
        if (StringUtil.isEmptyOrSpaces(filePath) || startHotReloadCallback == null)
            return;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                doStartWeex(filePath, startHotReloadCallback);
            }
        });
    }

    /**
     * for syncDoStartWeex
     *
     * @param filePath               the weex file path
     * @param startHotReloadCallback callback will be invoked after the weex file has been transformed
     */
    private void doStartWeex(String filePath, StartHotReloadCallback startHotReloadCallback) {
        if (weexProcess != null
                && filePath.equals(weexProcess.getWeexFilePath())
                && weexProcess.getProcess() != null) {
            startHotReloadCallback.startOk(weexProcess);
        }

        long prePort = 0;
        long wsPort = 0;

        if (weexProcess != null) {
            weexProcess.destory();
            prePort = weexProcess.getPreviewServerPort();
            wsPort = weexProcess.getWebServicePort();
            weexProcess = null;
        }

        long processId = Thread.currentThread().getId();

        Process process = null;

        if (WeexUtils.isPortHasBeenUsed(prePort))
            prePort = generatePreviewPort(processId);

        if (WeexUtils.isPortHasBeenUsed(wsPort))
            wsPort = generateWSPort(processId);

        try {
            String cmd = WeexAppConfig.INSTANCE.getNodeInstallPath() + File.separator + "weex --port %d --wsport %d --host %s --qr %s";

            String realCmd = String.format(cmd, prePort, wsPort, WeexAppConfig.INSTANCE.getLocalHostIP(false), filePath);

            WeexUtils.println(realCmd);
            process = Runtime.getRuntime().exec(realCmd);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (process == null) {
            WeexUtils.println("doStartWeex and then process is null");
            return;
        }


        WeexUtils.println("httpport: " + prePort);
        WeexUtils.println("wsPort: " + wsPort);

        weexProcess = new WeexProcess.Builder(process).previewServerPort(prePort).webServicePort(wsPort).weexFileName(filePath).build();

        startHotReloadCallback.startOk(weexProcess);


        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long generatePreviewPort(long threadId) {
        long port = 8000 + threadId;
        while (WeexUtils.isPortHasBeenUsed(port)) {
            port += 50;
        }
        return port;
    }

    private long generateWSPort(long threadId) {
        long port = 9000 + threadId;
        while (WeexUtils.isPortHasBeenUsed(port)) {
            port += 50;
        }
        return port;
    }


    public interface StartHotReloadCallback {
        void startOk(WeexProcess process);
    }


    public void stopWeexToolKitServer() {
        if (weexProcess != null)
            weexProcess.destory();
    }

}
