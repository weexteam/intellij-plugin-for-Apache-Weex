package com.darin.weex.ui.preview;

import com.darin.weex.language.WeexFileType;
import com.darin.weex.language.WeexLanguage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.PossiblyDumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darin on 5/18/16.
 */
public class WeexEditorProvider implements FileEditorProvider, PossiblyDumbAware {
    public static final String EDITOR_TYPE_ID = WeexLanguage.NAME + "FxPreviewEditor";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return WeexFileType.isWeexLanguage(virtualFile);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new WeexFxPreviewEditor(project, virtualFile);
    }

    @Override
    public void disposeEditor(@NotNull FileEditor fileEditor) {

    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {

    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
