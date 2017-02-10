package com.darin.weex.ui.actions.editorLayout;


import com.darin.weex.ui.preview.WeexFxPreviewEditor;

public class WeexEditorOnlyLayoutChangeAction extends WeexBaseChangeSplitLayoutAction {
  protected WeexEditorOnlyLayoutChangeAction() {
    super(WeexFxPreviewEditor.SplitEditorLayout.EDITOR_ONLY);
  }
}
