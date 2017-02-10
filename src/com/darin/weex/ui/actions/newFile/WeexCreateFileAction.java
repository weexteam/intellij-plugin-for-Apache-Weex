package com.darin.weex.ui.actions.newFile;

import com.darin.weex.language.WeexIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

/**
 * Created by darin on 8/18/16.
 */
public class WeexCreateFileAction extends CreateFileFromTemplateAction implements DumbAware {
    public static final String FILE_TEMPLATE = "Weex File";
    public static final String APPLICATION_TEMPLATE = "Weex Application";

    private static final String NEW_WEEX_FILE = "New Weex File";

    public WeexCreateFileAction() {
        super(NEW_WEEX_FILE, "", WeexIcons.ICON);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(NEW_WEEX_FILE)
                .addKind("Empty file", WeexIcons.ICON, FILE_TEMPLATE)
                .addKind("Simple Application", WeexIcons.ICON, APPLICATION_TEMPLATE);

    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s, String s1) {
        return NEW_WEEX_FILE;
    }
}
