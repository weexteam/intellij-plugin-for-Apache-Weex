package com.darin.weex.language;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.TemplateLanguageFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by darin on 5/18/16.
 */
public class WeexFileType extends LanguageFileType implements TemplateLanguageFileType {
    public static final LanguageFileType INSTANCE = new WeexFileType();
    @NonNls
    public static final String DEFAULT_EXTENSION = "we";
    @NonNls
    public static final String DOT_DEFAULT_EXTENSION = "." + DEFAULT_EXTENSION;

    private WeexFileType() {
        super(HTMLLanguage.INSTANCE);
    }

    protected WeexFileType(Language lang) {
        super(lang);
    }

    @NotNull
    @Override
    public String getName() {
        return "Weex";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Weex script";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return WeexIcons.ICON;
    }


    public static boolean isWeexLanguage(VirtualFile file) {
        if (file == null)
            return false;

        FileType fileType = file.getFileType();
        if (fileType == null)
            return false;

        boolean isWeexFileType = fileType.getDefaultExtension().equals(DEFAULT_EXTENSION) ||
                fileType.getName().equals(DEFAULT_EXTENSION);

        String fileExtension = file.getExtension();
        if (StringUtil.isEmpty(fileExtension))
            return false;
        boolean isWeexFile = file.getExtension().equals(DEFAULT_EXTENSION);
        return isWeexFile || isWeexFileType;
    }


}
