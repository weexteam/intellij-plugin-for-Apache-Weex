package com.darin.weex.utils;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.weexToolKit.WeexProcess;
import com.darin.weex.weexToolKit.WeexToolKit;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.util.concurrent.Future;

import static com.darin.weex.WeexAppConfig.LOCAL_IP;
import static com.darin.weex.utils.WeexCmd.AsyncRunCmd;
import static com.darin.weex.utils.WeexCmd.SyncRunCmd;

/**
 * Created by darin on 10/9/16.
 */
public class WeexSdk {

    //http://192.168.31.154:9999/index.html?hot-reload_controller&page=TC_Video_Update.js&loader=xhr
    private static final String WEEX_TOOLKIT_PREVIEW_URL_FOR_WEBVIEW = "http://%s:%d/index.html?page=%s.js&loader=xhr";
    private static final String WEEX_TOOLKIT_JS_URL_FOR_WEBVIEW = "http://%s:%d/%s.js";


    private static final String WEEX_TOOLKIT_JS_URL_FOR_QRCODE = "http://%s:%d/weex_tmp/h5_render/%s.js?wsport=%d";
    private static final String WEEX_TOOLKIT_PREVIEW_URL_FOR_QRCODE = "http://%s:%d/weex_tmp/h5_render/?hot-reload_controller&page=%s.js&loader=xhr";


    private static ServerWay mCurrentServerWay;

    private static WeexSdk intance;


    private Future weexServerThread;
    private int defaultWeexServerPort = 5678;

    private WeexSdk() {
        getCurrentServerWay(false);
    }


    public static WeexSdk getIntance() {
        if (intance == null)
            intance = new WeexSdk();
        return intance;
    }


    /**
     * Only One way left
     */
    public enum ServerWay {
        WEEX_TOOL_KIT(WEEX_TOOLKIT_PREVIEW_URL_FOR_WEBVIEW, WEEX_TOOLKIT_JS_URL_FOR_WEBVIEW, WEEX_TOOLKIT_PREVIEW_URL_FOR_QRCODE, WEEX_TOOLKIT_JS_URL_FOR_QRCODE);

        public String getPreviewUrl() {
            return previewUrl;
        }

        public String getJsUrl() {
            return jsUrl;
        }

        private final String previewUrl;
        private final String jsUrl;

        public String getQrCodePreviewUrl() {
            return qrCodePreviewUrl;
        }

        public String getQrCodeJsUrl() {
            return qrCodeJsUrl;
        }

        private final String qrCodePreviewUrl;
        private final String qrCodeJsUrl;


        ServerWay(String previewUrl, String jsUrl, String qrCodePreviewUrl, String qrCodeJsUrl) {
            this.previewUrl = previewUrl;
            this.jsUrl = jsUrl;
            this.qrCodePreviewUrl = qrCodePreviewUrl;
            this.qrCodeJsUrl = qrCodeJsUrl;
        }
    }

    /**
     * check the weex installed path is valid or not
     *
     * @param path the select path
     * @return true the path is valid or invalid
     */
    private boolean isWeexToolKitInstalled(String path) {
        if (StringUtil.isEmpty(path))
            return false;

        File file = new File(path);
        return file.exists() && new File(file, "weex").exists();
    }

    /**
     * @param reuse whether using pre set status of building method
     * @return current building method
     */
    public ServerWay getCurrentServerWay(boolean reuse) {

        if (reuse && mCurrentServerWay != null)
            return mCurrentServerWay;


        return mCurrentServerWay = ServerWay.WEEX_TOOL_KIT;
    }

    public boolean isWeexToolKitReady() {
        return getCurrentServerWay(true).equals(ServerWay.WEEX_TOOL_KIT);
    }


    /**
     * @param filePath               the weex file's name
     * @param isWebview              for show or hot reload
     * @param startHotReloadCallback
     * @return display this weex file in web view by this url
     */
    public String getPreviewUrl(String filePath, boolean isWebview, WeexToolKit.StartHotReloadCallback startHotReloadCallback) {
        WeexUtils.println("getPreviewUrl" + filePath);
        String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".we"));
        switch (mCurrentServerWay) {
            case WEEX_TOOL_KIT:
                if (isWebview)
                    return String.format(mCurrentServerWay.getPreviewUrl(), WeexAppConfig.getINSTANCE().getLocalHostIP(false), getDefaultWeexServerPort(), name);

                //HotReload
                WeexProcess process = WeexToolKit.getInstance().getWeexProcess();
                if (process == null || !filePath.equals(process.getWeexFilePath())) {

                    WeexToolKit.getInstance().syncDoStartWeex(filePath, startHotReloadCallback);

                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * @param filePath               the weex file's name
     * @param isStatic               whether this url is for previewing
     * @param startHotReloadCallback startHotReloadCallback will be invoked after start the weex server
     * @param useHostIp
     * @return display the weex file's javascript by this url
     */
    public String getJsUrl(String filePath, boolean isStatic, WeexToolKit.StartHotReloadCallback startHotReloadCallback, boolean useHostIp) {

        String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".we"));
        String ip = LOCAL_IP;
        if (useHostIp)
            ip = WeexAppConfig.getINSTANCE().getLocalHostIP(false);
        switch (mCurrentServerWay) {
            case WEEX_TOOL_KIT: {
                if (isStatic)
                    return String.format(mCurrentServerWay.getJsUrl(), ip, getDefaultWeexServerPort(), name);

                WeexProcess process = WeexToolKit.getInstance().getWeexProcess();

                if (process == null || !filePath.equals(process.getWeexFilePath())) {
                    WeexToolKit.getInstance().syncDoStartWeex(filePath, startHotReloadCallback);
                    return null;
                }

                return String.format(mCurrentServerWay.getQrCodeJsUrl(), ip, process.getPreviewServerPort(), name, process.getWebServicePort());
            }
            default:
                return null;

        }
    }


    /**
     * transform the given weex script file to javascript
     *
     * @param weexScript the weex script path
     * @param callback   callback will be invoked after transform completely
     */
    public void transform(String weexScript, WeexCmd.CmdExecuteCallback callback) {
        String transformCmd;
        if (false && WeexSdk.getIntance().isWeexToolKitReady()) {
            transformCmd = WeexAppConfig.getINSTANCE().getNodeInstallPath()
                    + File.separator
                    + "weex "
                    + WeexAppConfig.addDoubleQuotationMarks(weexScript)
                    + " -o "
                    + WeexAppConfig.addDoubleQuotationMarks(WeexAppConfig.TEMP_JS_FILE);
        } else {
            //Todo 自己编译模式
            transformCmd = addNodePathToCmd(WeexAppConfig.EXE_TRANSFORMER_FILE)
                    + " "
                    + WeexAppConfig.addDoubleQuotationMarks(weexScript)
                    + " -o "
                    + WeexAppConfig.addDoubleQuotationMarks(WeexAppConfig.TEMP_JS_FILE);
        }
        AsyncRunCmd(transformCmd, callback, null, true);
    }


    public int getDefaultWeexServerPort() {
        return defaultWeexServerPort;
    }


    /**
     * get a unused port number
     *
     * @return usable port number
     */
    private int generateWeexServerPoat() {
        while (WeexUtils.isPortHasBeenUsed(defaultWeexServerPort)) {
            defaultWeexServerPort++;
        }
        return defaultWeexServerPort;
    }


    /**
     * /usr/local/bin/node or "C:\\Program Files\\nodejs\\node.exe"
     *
     * @param cmd cmd
     * @return real cmd string
     */
    private static String addNodePathToCmd(String cmd) {

        String nodePath = WeexAppConfig.addDoubleQuotationMarks(WeexAppConfig.getINSTANCE().getNodeInstallPath() + File.separator + WeexAppConfig.getNodeRealName());

        return nodePath + " " + cmd;
    }

    /**
     * start the npm serve with run start shell script.
     * maybe the 'npm run serve' cmd is enough cause we will transform the weex script manually
     *
     * @param callback
     */
    public void startServe(WeexCmd.CmdExecuteCallback callback) {
        stopServe();
        String startServerCmd;
//        if (false && WeexSdk.getIntance().isWeexToolKitReady()) {
//            startServerCmd = WeexAppConfig.getINSTANCE().getNodeInstallPath() + File.separator + "weex --port " + generateWeexServerPoat() + " --server " + WeexAppConfig.TEMP_JS_FILE;
//        } else {
        startServerCmd = addNodePathToCmd(WeexAppConfig.EXE_HTTP_SERVER_FILE) + " -p " + generateWeexServerPoat() + " " + WeexAppConfig.TEMP_JS_FILE;
//        }

        WeexUtils.println(startServerCmd);

        weexServerThread = AsyncRunCmd(startServerCmd, callback, null, true);
    }

    /**
     * kill all the weex serve
     * <p>
     * see resource/shells/stopServe script, we has wrote it into loacal path(PathManager.getConfigPath())
     * <p>
     * then run "sh stopServe"
     * <p>
     * <p>
     * the server will be shutdown after the cmd threadPool shutdown
     */
    public void stopServe() {
        if (WeexSdk.getIntance().isWeexToolKitReady()) {
            WeexToolKit.getInstance().stopWeexToolKitServer();
        }

        if (weexServerThread != null) {
            weexServerThread.cancel(true);
            weexServerThread = null;
        }

        WeexCmd.shutdown();
        if (!WeexUtils.isWindows()) {
            String path = WeexAppConfig.DEFAULT_CONFIG_PATH;
            WeexAppConfig.getINSTANCE().initStopServeShell(false);

            File stopServeFile = new File(path + File.separator + "stopServe");
            SyncRunCmd("sh " + stopServeFile.getPath(), false, null);
        }

    }

}
