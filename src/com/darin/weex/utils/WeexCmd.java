package com.darin.weex.utils;

import com.darin.weex.WeexAppConfig;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.impl.ContentImpl;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by darin on 10/9/16.
 */
public class WeexCmd {
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static String mNpmInstallPath = null;
    private static String[] mEnvironment;
    private static ConsoleView mConsoleView;
    private static ToolWindow mWindow;

    /**
     * We should add npm installed path into the runtime environment path
     *
     * @return the runtime environment paths of this plugin
     */
    public static String[] getEnv() {

        if (!StringUtil.isEmpty(mNpmInstallPath) && mNpmInstallPath.equals(WeexAppConfig.getINSTANCE().getNoedInstallPath()))
            return mEnvironment;

        mNpmInstallPath = WeexAppConfig.getINSTANCE().getNoedInstallPath();

        final Map<String, String> env = System.getenv();
        mEnvironment = new String[env.size()];

        int i = 0;
        Iterator<Map.Entry<String, String>> entryIterator = env.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            String value = entry.getValue();
            String key = entry.getKey();
            if (key.equals("PATH"))
                value += ":" + WeexAppConfig.getINSTANCE().getNoedInstallPath();
            mEnvironment[i] = key + "=" + value;
            i++;
        }

        return mEnvironment;
    }

    public interface CmdExecuteCallback {
        void done(String processResult);
    }

    public static Future<?> AsyncRunCmd(final String cmd, final CmdExecuteCallback callback, final File workDir, final boolean showConsole) {
        return threadPool.submit(new Runnable() {
            @Override
            public void run() {
                String result = SyncRunCmd(cmd, showConsole, workDir);
                if (callback != null)
                    callback.done(result);
            }
        });
    }

    /**
     * @param cmd         the cmd that will be executed
     * @param showConsole show cmd executed string on console or not
     * @param workDir
     */
    public static String SyncRunCmd(String cmd, boolean showConsole, File workDir) {
        WeexUtils.println(cmd);
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec(cmd, getEnv(), workDir);
            //process.waitFor();
            result = inputStreamToString(process.getInputStream(), true);
        } catch (Exception e1) {
            //e1.printStackTrace();
            //WeexAppConfig.getINSTANCE().init();
        }
        if (showConsole && !StringUtil.isEmpty(result)) {
            printLoginConsole(result);
        }


        WeexUtils.println("Done + " + result);
        return result;
    }

    /**
     * @param inputStream the inputstream that will be transformed to String text
     * @param rn          if rn is true, we will add \n at the end of each line
     * @return the Strint text that read from the inputStream
     */
    @NotNull
    public static String inputStreamToString(InputStream inputStream, boolean rn) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder s = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                s.append(line);
                if (rn)
                    s.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s.toString();
    }

    public static void shutdown() {
        threadPool.shutdownNow();
        threadPool = Executors.newCachedThreadPool();
    }

    /**
     * show console view in the tool window
     *
     * @param msg the message that will be shown in the tool window.
     */
    public static void printLoginConsole(String msg) {
        /**
         * ConsoleViewContentType.USER_INPUT green
         *
         * ConsoleViewContentType.SYSTEM_OUTPUT blue
         *
         * ConsoleViewContentType.ERROR_OUTPUT red
         *
         * ConsoleViewContentType.NORMAL_OUTPUT black
         */

        if (mConsoleView == null || msg == null)
            return;
        ConsoleViewContentType type;

        if (msg.contains("NOTE")) {
            type = ConsoleViewContentType.USER_INPUT;
        } else if (msg.contains("ERROR")) {
            type = ConsoleViewContentType.ERROR_OUTPUT;
        } else if (msg.contains("WARNING")) {
            type = ConsoleViewContentType.SYSTEM_OUTPUT;
        } else {
            type = ConsoleViewContentType.NORMAL_OUTPUT;
        }
        try {
            mConsoleView.print(msg, type);
        } catch (RejectedExecutionException e) {
            mConsoleView = null;
        }

    }

    private static ToolWindowManager mManager = null;

    public static void initConsoleView(Project project) {
        if (mConsoleView != null || project == null)
            return;
        mManager = ToolWindowManager.getInstance(project);

        if (mManager == null)
            return;

        String id = "Weex Console";

        TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
        TextConsoleBuilder builder = factory.createBuilder(project);
        mConsoleView = builder.getConsole();
        mWindow = mManager.getToolWindow(id);

        if (mWindow == null) {
            mWindow = mManager.registerToolWindow(id, false, ToolWindowAnchor.BOTTOM);
            mWindow.getContentManager().addContent(new ContentImpl(mConsoleView.getComponent(), "WeexConsole", true));
        }

        mWindow.show(new Runnable() {
            @Override
            public void run() {
                WeexUtils.println("Hello world");
            }
        });
    }

    public static void destroyConsoleView() {
        if (mManager != null) {
            mManager.unregisterToolWindow("Weex Console");
            mManager = null;
        }
        mWindow = null;
        if (mConsoleView != null) {
            mConsoleView.dispose();
        }
        mConsoleView = null;
    }
}
