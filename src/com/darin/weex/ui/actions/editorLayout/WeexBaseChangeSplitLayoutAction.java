package com.darin.weex.ui.actions.editorLayout;

import com.darin.weex.ui.preview.WeexFxPreviewEditor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Created by darin on 5/23/16.
 */
public class WeexBaseChangeSplitLayoutAction extends AnAction implements DumbAware, Toggleable {
    @Nullable
    private final WeexFxPreviewEditor.SplitEditorLayout myLayoutToSet;

    protected WeexBaseChangeSplitLayoutAction(@Nullable WeexFxPreviewEditor.SplitEditorLayout layoutToSet) {
        myLayoutToSet = layoutToSet;
    }


    @Override
    public void update(AnActionEvent e) {
        super.update(e);


        final WeexFxPreviewEditor splitFileEditor = findSplitEditor(e);
        e.getPresentation().setEnabled(splitFileEditor != null);

        if (myLayoutToSet != null && splitFileEditor != null) {
            e.getPresentation().putClientProperty(SELECTED_PROPERTY, splitFileEditor.getCurrentEditorLayout().equals(myLayoutToSet));
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        anActionEvent.getPresentation().putClientProperty(SELECTED_PROPERTY, true);

        Project project = anActionEvent.getProject();

        WeexFxPreviewEditor edit = findSplitEditor(anActionEvent);
        if (edit != null)
            edit.rePaintView(myLayoutToSet);
    }


    @Nullable
    public WeexFxPreviewEditor findSplitEditor(AnActionEvent e) {
        final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
        if (editor instanceof WeexFxPreviewEditor) {
            return (WeexFxPreviewEditor) editor;
        } else {
            return WeexFxPreviewEditor.Companion.getPARENT_SPLIT_KEY().get(editor);
        }
    }
}
