package com.darin.weex.ui.actions.editorLayout;


import com.darin.weex.ui.preview.WeexFxPreviewEditor;

public class WeexPreviewOnlyLayoutChangeAction extends WeexBaseChangeSplitLayoutAction {
    protected WeexPreviewOnlyLayoutChangeAction() {
        super(WeexFxPreviewEditor.SplitEditorLayout.PREVIEW_ONLY);
    }
}
