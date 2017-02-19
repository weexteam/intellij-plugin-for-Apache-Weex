package com.darin.weex;


import com.darin.weex.language.WeexFileChangeAdapter;
import com.darin.weex.utils.WeexCmd;
import com.darin.weex.utils.WeexConstants;
import com.darin.weex.utils.WeexSdk;
import com.darin.weex.utils.WeexUtils;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.*;
import java.net.InetAddress;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.darin.weex.utils.WeexCmd.*;
import static com.darin.weex.utils.WeexUtils.startCheckServerStatus;

/**
 * Created by darin on 5/23/16.
 */
public class WeexAppConfig extends Properties {

    public static WeexAppConfig getINSTANCE() {
        return INSTANCE;
    }

    private static WeexAppConfig INSTANCE = new WeexAppConfig();

    private static String CONFIG_PATH;

    /**
     * properties configuration keys
     */
    private static final String KEY_GITHUB_PATH = "build-path";
    private static final String KEY_NOED_INSTALL_PATH = "node-install-path";
    private static final String KEY_SPLIT_PROPORTION = "split-proportion";
    private static final String KEY_TRANSFORMER_PATH = "transformer-path";
    private static final String KEY_WEBVIEW_WIDTH = "web-width";
    private static final String KEY_WEBVIEW_HEIGHT = "web-height";


    /**
     * the default node install path
     */
    public static final String DEFAULT_NODE_PATH = "/usr/local/bin";
    private static final String DEFAULT_NODE_PATH_WIN = "C:\\Program Files\\nodejs";


    private static final String NODE_NAME = "node";
    private static final String NODE_NAME_WIN = NODE_NAME + ".exe";


    /**
     * for weex tool-kit
     */
    public static String DEFAULT_CONFIG_PATH;

    public static String TEMP_JS_FILE;

    public static String EXE_HTTP_SERVER_FILE;
    public static String EXE_TRANSFORMER_FILE;


    private static final String renderFilePath = "/render/render.zip";
    private static final String serverFilePath = "/render/server.zip";
    private static final String transformerFilePath = "/render/transformer.zip";

    /**
     * debug option switch
     */
    public static final boolean isDebug = false;

    private final WeexFileChangeAdapter weexFileChangeAdapter = new WeexFileChangeAdapter();

    private WeexAppConfig() {

        initPaths();

        File file = new File(CONFIG_PATH);

        try {
            if (!file.exists())
                if (!file.createNewFile())
                    throw new IllegalAccessError("Can't create config file");

            load(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return node in Mac or linux, node.exe in Windows
     */
    public static String getNodeRealName() {
        return WeexUtils.isWindows() ? WeexAppConfig.NODE_NAME_WIN : WeexAppConfig.NODE_NAME;
    }

    /**
     * @return "/usr/local/bin" or "C:\\Program Files\\nodejs"
     */
    public static String getNodeRealPath() {
        return WeexUtils.isWindows() ? WeexAppConfig.DEFAULT_NODE_PATH_WIN : WeexAppConfig.DEFAULT_NODE_PATH;
    }

    /**
     * The cmd will not be executed in Windows if one parameter of the cmd has space
     *
     * @param string cmd parameter
     * @return cmd parameter with Double quotation  such as make C:\\Program Files\\nodejs\\node.exe ->  "C:\\Program Files\\nodejs\\node.exe"
     */
    public static String addDoubleQuotationMarks(String string) {
        if (WeexUtils.isWindows() && !string.startsWith("\""))
            return "\"" + string + "\"";
        return string;
    }

    /**
     * @param string
     * @return
     */
    private static String cutUselessMark(String string) {
        return string.replace("\\", "");
    }


    /**
     * initialize the paths of server, jsFile, transformer..
     */
    private void initPaths() {

        DEFAULT_CONFIG_PATH = PathManager.getConfigPath() + File.separator + "weex-tool";

        TEMP_JS_FILE = new File(DEFAULT_CONFIG_PATH, "weex-html5").getAbsolutePath();

        EXE_HTTP_SERVER_FILE = addDoubleQuotationMarks(new File(DEFAULT_CONFIG_PATH, "serve/bin/serve").getAbsolutePath());

        EXE_TRANSFORMER_FILE = addDoubleQuotationMarks(new File(DEFAULT_CONFIG_PATH, "weex-transformer/bin/transformer.js").getAbsolutePath());

        WeexUtils.println(TEMP_JS_FILE);

        CONFIG_PATH = PathManager.getConfigPath() + File.separator + "options/weex.properties";

        File weex_tool = new File(DEFAULT_CONFIG_PATH);
        if (weex_tool.exists())
            FileUtil.delete(weex_tool);


        weex_tool.mkdirs();

        initOutputPath();
        /**
         * un zip render && transformer && server
         */

        WeexUtils.unzip(this.getClass().getResourceAsStream(transformerFilePath), WeexAppConfig.DEFAULT_CONFIG_PATH);

        WeexUtils.unzip(this.getClass().getResourceAsStream(renderFilePath), WeexAppConfig.DEFAULT_CONFIG_PATH);

        WeexUtils.unzip(this.getClass().getResourceAsStream(serverFilePath), WeexAppConfig.DEFAULT_CONFIG_PATH);


        //setPermission();

    }

    private void setPermission() {
        WeexCmd.SyncRunCmd("chmod -R 777 " + WeexAppConfig.DEFAULT_CONFIG_PATH + "/*", false, null);
    }

    /**
     * create the default temp script output path
     */
    private boolean initOutputPath() {

        File weexJsOutputPath = new File(WeexAppConfig.TEMP_JS_FILE);

        if (weexJsOutputPath.exists())
            FileUtil.delete(weexJsOutputPath);

        return weexJsOutputPath.mkdirs();
    }


    /**
     * init server
     */
    void init() {
        VirtualFileManager.getInstance().addVirtualFileListener(weexFileChangeAdapter);

        if (isNodePathValid(getNodeInstallPath())) {
            setNodeInstallPath(getNodeInstallPath());
        } else if (isNodePathValid(getNodeRealPath())) {
            setNodeInstallPath(getNodeRealPath());
        }


        initStopServeShell(true);

        WeexSdk.getIntance().startServe(null);

        startCheckServerStatus();

        try {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            if (projects.length > 0) {
                WeexCmd.initConsoleView(projects[0]);
            }
        } catch (Exception exception) {
            destroyConsoleView();
        }

    }


    void destroy() {
        VirtualFileManager.getInstance().removeVirtualFileListener(weexFileChangeAdapter);

        WeexSdk.getIntance().stopServe();

        /**
         * project has been changed,
         * so we should init the console view again according to the new project
         */
        destroyConsoleView();

        save();
    }


    public void setSplitProportion(float proportion) {
        String proportionString = String.valueOf(proportion);
        setProperty(KEY_SPLIT_PROPORTION, proportionString);
    }

    public float getSplitProportion() {
        return Float.valueOf(getProperty(KEY_SPLIT_PROPORTION, "0.5"));
    }

    public String getNodeInstallPath() {
        return getProperty(KEY_NOED_INSTALL_PATH, getNodeRealPath());
    }

    public void setNodeInstallPath(String path) {
        setPropertyAndSave(KEY_NOED_INSTALL_PATH, path);
    }


    public void setWebviewWidth(int width) {
        setPropertyAndSave(KEY_WEBVIEW_WIDTH, String.valueOf(width));
    }

    public void setWebviewHeight(int height) {
        setPropertyAndSave(KEY_WEBVIEW_HEIGHT, String.valueOf(height));
    }


    public int getWebviewWidth() {
        return Integer.valueOf(getProperty(KEY_WEBVIEW_WIDTH, String.valueOf(669)));
    }

    public int getWebviewHeight() {
        return Integer.valueOf(getProperty(KEY_WEBVIEW_HEIGHT, String.valueOf(1200)));
    }


    /**
     * @return the Weex installed path that has been set
     */
    public String getWeexGitHubPath() {
        return getProperty(KEY_GITHUB_PATH, "");
    }

    /**
     * override setProperty, after set property,save it to local file automatically
     *
     * @param key   property key
     * @param value property value
     */
    private void setPropertyAndSave(String key, String value) {
        setProperty(key, value);
        save();
    }


    /**
     * check the node installed path is valid or not
     *
     * @param path the select path
     * @return true the path is valid or invalid
     */
    public boolean isNodePathValid(String path) {
        if (StringUtil.isEmpty(path))
            return false;

        File file = new File(path);
        if (!file.exists())
            return false;

        File[] files = file.listFiles();

        if (files == null)
            return false;

        String name;

        for (File f : files) {

            name = f.getName();

            if (name.equals(NODE_NAME) || name.equals(NODE_NAME_WIN)) {
                return true;
            }
        }

        return false;
    }


    private void save() {
        try {
            store(new FileOutputStream(CONFIG_PATH), "config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long lastTime = 0;
    public static String LOCAL_IP = "127.0.0.1";
    private static String mCurrentIp = null;

    /**
     * @param forceUpdate force to get local ip
     * @return local ip
     */
    public String getLocalHostIP(boolean forceUpdate) {
        long currentTime = System.currentTimeMillis();

        if (forceUpdate || StringUtil.isEmpty(mCurrentIp)) {
            getLocalIp();
            lastTime = System.currentTimeMillis();
        } else {
            //ip 缓存时间
            long cacheTime = 60 * 1000;
            boolean needUpdate = (currentTime - lastTime) > cacheTime;
            if (needUpdate) {
                getLocalIp();
                lastTime = System.currentTimeMillis();
            }
        }
        return mCurrentIp;
    }

    private String getLocalIp() {
        mCurrentIp = getLocalHostIpFromCmd();
        if (StringUtil.isEmpty(mCurrentIp))
            mCurrentIp = getLocalHostIpFromJava();
        return mCurrentIp;
    }

    private String getLocalHostIpFromJava() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (Exception ex) {
            return LOCAL_IP;
        }
    }

    private static String IPREG = "(?<=inet ).*(?= netmask)";
    private static Pattern ipPattern = Pattern.compile(IPREG);

    /**
     * get local host ip through cmd
     *
     * @return the ip
     */
    private String getLocalHostIpFromCmd() {
        String ifconfig = SyncRunCmd(WeexConstants.CMD_GET_IP, false, null).trim();
        Matcher m = ipPattern.matcher(ifconfig);
        String ip = null;
        try {
            while (m.find()) {
                ip = m.group();
                // except 127.0.0.1
                if (!ip.contains(LOCAL_IP)) {
                    return ip;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    /**
     * init stop shell script from "/shells/stopServe"
     *
     * @param forceUpdate force rewrite stop server shell script
     * @return true means rewriting stop server shell script is successfully or not
     */
    public boolean initStopServeShell(boolean forceUpdate) {
        String path = WeexAppConfig.DEFAULT_CONFIG_PATH;
        File stopServeFile = new File(path + File.separator + "stopServe");


        if (stopServeFile.exists() && !forceUpdate) {
            return true;
        }
        String stopServe = inputStreamToString(WeexUtils.class.getResourceAsStream("/shells/stopServe"), true);

        try {
            FileWriter f = new FileWriter(stopServeFile);
            f.write(stopServe);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


}
