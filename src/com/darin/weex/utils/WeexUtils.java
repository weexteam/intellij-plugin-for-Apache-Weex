package com.darin.weex.utils;

import com.darin.weex.WeexAppConfig;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.darin.weex.utils.WeexCmd.SyncRunCmd;


/**
 * Created by darin on 5/23/16.
 */
public class WeexUtils {
    private static ArrayList<onLocalServerStatusChangeListener> listerners = new ArrayList<onLocalServerStatusChangeListener>();
    private static boolean lastStatus = false;


    /**
     * inner function to save test logs
     *
     * @param msg
     */
    private static void savelocal(String msg) {
        if (!WeexAppConfig.isDebug)
            return;
        try {
            FileWriter f = new FileWriter(new File(WeexAppConfig.DEFAULT_CONFIG_PATH, "debug.txt"), true);
            f.write(msg);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * whether the port has been used
     *
     * @param port port number
     * @return true or not
     */
    public static boolean isPortHasBeenUsed(long port) {
        if (port <= 0)
            return true;


        if (WeexUtils.isWindows()) {
            String result = SyncRunCmd("netstat -aon", false, null);
            return result.contains(String.valueOf(port));
        } else {
            return !StringUtil.isEmpty(SyncRunCmd("lsof -i tcp:" + port, false, null));
        }

    }

    /**
     * judge whether the local server is on
     * may has a better way to do this
     *
     * @return true means local server is on
     */
    public static boolean isServerOn() {
        return !WeexUtils.isWindows() && SyncRunCmd("lsof -i tcp:12580", false, null).contains("TCP *:12580 (LISTEN)");
    }


    /**
     * unzip zip file
     *
     * @param zip        the zip input stream
     * @param outputPath outputPath of zip files
     * @return success or not
     */
    public static boolean unzip(InputStream zip, String outputPath) {
        boolean isUnzipOk = true;
        try {
            ZipInputStream Zin = new ZipInputStream(zip);//输入源zip路径
            BufferedInputStream Bin = new BufferedInputStream(Zin);
            File outputFile;
            ZipEntry entry;
            while ((entry = Zin.getNextEntry()) != null) {
                outputFile = new File(outputPath, entry.getName());
                if (entry.isDirectory()) {
                    continue;
                } else {
                    File parent = new File(outputFile.getParent());
                    if (!parent.exists())
                        if (!parent.mkdirs())
                            return false;
                }


                FileOutputStream out = new FileOutputStream(outputFile);
                BufferedOutputStream Bout = new BufferedOutputStream(out);
                int b;
                while ((b = Bin.read()) != -1) {
                    Bout.write(b);
                }
                Bout.close();
                out.close();
            }
            Bin.close();
            Zin.close();
        } catch (Exception e) {
            e.printStackTrace();
            isUnzipOk = false;
        }

        return isUnzipOk;
    }

    /**
     * unzip zip file
     *
     * @param zip        the unzip file's path
     * @param outputPath outputPath of zip files
     * @return success or not
     */
    public static boolean unzip(String zip, String outputPath) {
        try {
            return unzip(new FileInputStream(zip), outputPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * set the npm installed path
     *
     * @param component the parent container of the choose component
     * @return the selected npm installed path
     */
    public static String chooseNpmPath(Component component) {
        String path = choosePath();

        if (StringUtil.isEmpty(path))
            return null;

        if (!WeexAppConfig.getINSTANCE().isNodePathValid(path)) {
            WeexShow.showMessage("the path is invalid");
            return null;
        }
        return path;
    }


    /**
     * @return
     */
    private static String choosePath() {
        String path = null;
        Project project = ProjectManager.getInstance().getDefaultProject();
        VirtualFile chooseedFile = FileChooser.chooseFile(new FileChooserDescriptor(false, true, false, false, false, false), project, project.getBaseDir());

        if (chooseedFile != null) {
            path = chooseedFile.getPath();
        }

        System.out.println(path);
        return path;

    }

    /**
     * whether http server is running
     */
    public static void startCheckServerStatus() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (!this.isInterrupted()) {
                    boolean currentStatus;

                    try {

                        sleep(1000 * 5);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (WeexSdk.getIntance().isWeexToolKitReady()) {
                        currentStatus = WeexUtils.isPortHasBeenUsed(WeexSdk.getIntance().getDefaultWeexServerPort());
                    } else {
                        currentStatus = WeexUtils.isServerOn();
                    }

                    if (lastStatus == currentStatus) {
                        continue;
                    }

                    for (onLocalServerStatusChangeListener listerner : listerners)
                        listerner.onLocalServerStatusChange(currentStatus);

                    lastStatus = currentStatus;
                }
            }
        };


        thread.setPriority(Thread.MIN_PRIORITY);
        /**
         * make it be daemon
         */
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * will call {@link onLocalServerStatusChangeListener#onLocalServerStatusChange} if the http server's status is changed
     *
     * @param listener listener
     */
    public static void addServerChangeListener(onLocalServerStatusChangeListener listener) {
        if (listener != null)
            listerners.add(listener);
    }

    /**
     * remove the listener
     *
     * @param listener
     */
    public static void removeServerChangeListener(onLocalServerStatusChangeListener listener) {
        if (listener != null)
            listerners.remove(listener);
    }

    public interface onLocalServerStatusChangeListener {
        void onLocalServerStatusChange(boolean isOn);
    }


    /**
     * @return current environment is windows operator system
     */
    public static boolean isWindows() {
        return SystemInfo.isWindows;
    }


    public static void println(Object msg) {
        if (WeexAppConfig.isDebug && msg != null)
            System.out.println(msg);
    }

    public static void printStack() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        println("-----------------------------------");
        if (stackElements != null) {
            for (StackTraceElement stackElement : stackElements) {
                if (stackElement.getClassName().contains("weex")) {
                    println(stackElement.getClassName() + ":" + stackElement.getFileName() + ":" + stackElement.getLineNumber() + ":" + stackElement.getMethodName());
                }
            }
        }
        println("-----------------------------------");
    }

}
