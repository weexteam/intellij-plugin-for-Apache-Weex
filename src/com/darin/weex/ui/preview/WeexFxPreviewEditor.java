package com.darin.weex.ui.preview;

import com.darin.weex.WeexAppConfig;
import com.darin.weex.utils.TransformTasks;
import com.darin.weex.utils.WeexUtils;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.JBSplitter;
import com.intellij.util.Alarm;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by darin on 5/18/16.
 */
public class WeexFxPreviewEditor extends UserDataHolderBase implements TextEditor, FileEditor, Disposable {
    public static final Key<WeexFxPreviewEditor> PARENT_SPLIT_KEY = Key.create("parentSplit");

    private static final String PREVIEW_EDITOR_NAME = "HTML5";

    private final TextEditorProvider mMainEditorProvider = new PsiAwareTextEditorProvider();

    private final Alarm mUpdatePrevewWidthAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);
    /**
     * The {@link Document} previewed in this editor.
     */

    private VirtualFile mCurrentModifyFile;
    /**
     * 渲染组建
     */
    private JPanel mPreviewPanel;
    /**
     * 通过 {@link FileEditor} getComponent 来获取编辑框的组建
     */
    private FileEditor mMainEditor;
    /**
     * webview 容器
     */
    private JFXPanel mWebviewContainer;
    /**
     * editor 和 preview 容器
     */
    private JBSplitter splitter;
    /**
     * 最终的容器, 包括 editor,preview,toolbar
     */
    private JPanel mFinalView;
    private SplitEditorLayout mySplitEditorLayout = SplitEditorLayout.SPLIT;
    private WeexSplitEditorToolbar myToolbarWrapper;
    private WeexBrowser weexBrowser;
    private WeexUtils.onLocalServerStatusChangeListener listerner;
    private Runnable mUpdatePreviewWidthRunnable;

    private boolean mJfxInitOk = false;
    private boolean mWebViewInitOk = false;

    private static int webviewHeight = WeexAppConfig.getINSTANCE().getWebviewHeight();
    private static int webviewWidth = WeexAppConfig.getINSTANCE().getWebviewWidth();

    public WeexFxPreviewEditor(Project project, final VirtualFile file) {
        initData(project, file);
        initUi();
    }

    public WeexSplitEditorToolbar getMyToolbarWrapper() {
        return myToolbarWrapper;
    }

    private void initData(Project p, VirtualFile file) {
        this.mCurrentModifyFile = file;
        mMainEditor = mMainEditorProvider.createEditor(p, file);
        this.putUserData(PARENT_SPLIT_KEY, this);
    }

    private void initPreviewUi() {
        mWebviewContainer = new JFXPanelWrapper(); // initializing javafx
        mPreviewPanel.add(mWebviewContainer);
        rePaintView(mySplitEditorLayout);

        splitter = new JBSplitter(false, WeexAppConfig.getINSTANCE().getSplitProportion(), 0.15f, 0.85f);

        initWebview(webviewWidth, webviewHeight);
        splitter.setSplitterProportionKey("");

        splitter.setFirstComponent(mMainEditor.getComponent());
        splitter.setSecondComponent(mPreviewPanel);
        splitter.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (mUpdatePreviewWidthRunnable != null)
                    mUpdatePrevewWidthAlarm.cancelRequest(mUpdatePreviewWidthRunnable);

                JComponent second = splitter.getSecondComponent();
                final int width = second.getWidth();
                final int height = second.getHeight();

                if (webviewWidth == width)
                    return;

                WeexUtils.println("Width = " + width + ", height = " + height);

                mUpdatePreviewWidthRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (width != 0 && height != 0) {
                            webviewHeight = height;
                            webviewWidth = width;

                            initWebview(width, height);

                            WeexAppConfig.getINSTANCE().setSplitProportion(splitter.getProportion());
                            WeexAppConfig.getINSTANCE().setWebviewHeight(height);
                            WeexAppConfig.getINSTANCE().setWebviewWidth(width);

                        }
                    }
                };

                if (!mUpdatePrevewWidthAlarm.isDisposed())
                    mUpdatePrevewWidthAlarm.addRequest(mUpdatePreviewWidthRunnable, 20L);
            }
        });
        myToolbarWrapper = new WeexSplitEditorToolbar(splitter);
        mFinalView.remove(mMainEditor.getComponent());
        mFinalView.add(myToolbarWrapper, BorderLayout.NORTH);
        mFinalView.add(splitter, BorderLayout.CENTER);
        mFinalView.updateUI();
        listerner = new WeexUtils.onLocalServerStatusChangeListener() {
            @Override
            public void onLocalServerStatusChange(boolean isOn) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * update the server button
                         */
                        mFinalView.remove(myToolbarWrapper);
                        myToolbarWrapper = new WeexSplitEditorToolbar(splitter);
                        mFinalView.add(myToolbarWrapper, BorderLayout.NORTH);
                        mFinalView.updateUI();
                    }
                });
                if (isOn)
                    reLoad();
            }
        };
        mJfxInitOk = true;

        WeexUtils.addServerChangeListener(listerner);

        TransformTasks.instance.addTransformTask(mCurrentModifyFile);
    }


    private void initUi() {
        mFinalView = new JPanel(new BorderLayout());
        mFinalView.add(mMainEditor.getComponent());

        mPreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        /**
         * new JFXPanel() may block the ui thread, so we invoke it later
         */
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                initPreviewUi();
            }
        });

    }

    private void reLoad() {
        TransformTasks.instance.addLoadUrlTask(mCurrentModifyFile.getPath());
    }

    private void initWebviewWithComponent() {
        webviewWidth = (int) (mFinalView.getWidth() * (1 - splitter.getProportion()));
        initWebview(webviewWidth, mFinalView.getHeight());
    }


    private void initWebview(final int width, final int height) {
        if (mWebviewContainer == null)
            return;

        runLater(new Runnable() {
            @Override
            public void run() {
                weexBrowser = new WeexBrowser(width, height, mCurrentModifyFile.getPath());

                Scene scene = new Scene(weexBrowser, width, height, Color.web("#666970"));
                mWebviewContainer.setScene(scene);
                reLoad();
            }
        });
    }


    @NotNull
    @Override
    public JComponent getComponent() {
        return mFinalView;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return mFinalView;
    }

    @NotNull
    @Override
    public String getName() {
        return PREVIEW_EDITOR_NAME;
    }

    @NotNull
    @Override
    public FileEditorState getState(@NotNull FileEditorStateLevel fileEditorStateLevel) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {
    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    @Override
    public void dispose() {
        mMainEditor.dispose();
        WeexUtils.removeServerChangeListener(listerner);
    }

    /**
     * @return the TextEditor that we can edit code
     */
    @NotNull
    @Override
    public Editor getEditor() {
        return ((TextEditor) mMainEditor).getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return ((TextEditor) mMainEditor).canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        ((TextEditor) mMainEditor).navigateTo(navigatable);
    }

    /**
     * @return get current split layout
     */
    @NotNull
    public SplitEditorLayout getCurrentEditorLayout() {
        return mySplitEditorLayout;
    }

    /**
     * @return Text editor component
     */
    private JComponent getEditComponent() {
        return mMainEditor.getComponent();
    }

    /**
     * @return Preview edtior component
     */
    private JComponent getPriviewComponent() {
        return mPreviewPanel;
    }

    /**
     * reset the layout with the spliteditorlayout
     *
     * @param layout {@link SplitEditorLayout} the editor layout
     */
    public void rePaintView(SplitEditorLayout layout) {
        if (!mJfxInitOk)
            return;
        getEditComponent().setVisible(layout.showEditor);
        getPriviewComponent().setVisible(layout.showPreview);
        myToolbarWrapper.refresh();
        mySplitEditorLayout = layout;
    }

    private void runLater(Runnable runnable) {
        Platform.runLater(runnable);
    }

    /**
     * three layouts of split preview
     * 1. text area only
     * 2. preview only
     * 3. both text an preview
     */
    public enum SplitEditorLayout {
        EDITOR_ONLY(true, false, "Text"),
        PREVIEW_ONLY(false, true, "Preview"),
        SPLIT(true, true, "Split");

        public final boolean showEditor;
        public final boolean showPreview;
        public final String presentationName;

        SplitEditorLayout(boolean showEditor, boolean showPreview, String presentationName) {
            this.showEditor = showEditor;
            this.showPreview = showPreview;
            this.presentationName = presentationName;
        }

        @Override
        public String toString() {
            return presentationName;
        }
    }
}
