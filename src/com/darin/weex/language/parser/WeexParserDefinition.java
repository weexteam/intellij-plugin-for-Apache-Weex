package com.darin.weex.language.parser;

import com.darin.weex.language.WeexFile;
import com.darin.weex.language.WeexLanguage;
import com.darin.weex.language.lexer.WeexLexer;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darin on 12/01/2017.
 */
public class WeexParserDefinition extends HTMLParserDefinition {
    public static final IStubFileElementType elementType = new IStubFileElementType(WeexLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new WeexLexer();
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new WeexFile(viewProvider);
    }




    @Override
    public IFileElementType getFileNodeType() {
        return elementType;
    }
}
