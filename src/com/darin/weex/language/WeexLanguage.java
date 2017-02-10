package com.darin.weex.language;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darin on 5/18/16.
 */
public class WeexLanguage extends Language implements TemplateLanguage {
    @NonNls
    public static final WeexLanguage INSTANCE = new WeexLanguage();

    public static LanguageFileType getDefaultTemplateLang() {
        return StdFileTypes.HTML;
    }

    @NotNull
    final static public String NAME = "Weex";

    private WeexLanguage() {
        super(NAME);
    }
}
