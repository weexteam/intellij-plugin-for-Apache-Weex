package com.darin.weex.language.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.lexer._HtmlLexer;

/**
 * Created by darin on 12/01/2017.
 */
public class WeexLexer extends BaseHtmlLexer {
    public WeexLexer() {
        this(new MergingLexerAdapter(new FlexAdapter(new _HtmlLexer()), TOKENS_TO_MERGE), true);
    }

    protected WeexLexer(Lexer _baseLexer, boolean _caseInsensitive) {
        super(_baseLexer, _caseInsensitive);
    }

    @Override
    protected boolean isHtmlTagState(int state) {
        return false;
    }
}
