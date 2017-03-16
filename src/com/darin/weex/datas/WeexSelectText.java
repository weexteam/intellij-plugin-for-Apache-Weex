package com.darin.weex.datas;

import com.darin.weex.utils.WeexShow;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by darin on 5/17/16.
 */

/**
 * get the select text from the editor. we will return the all text of the file if the select text is null
 */
public class WeexSelectText {
    private String text;
    private VirtualFile file;


    public WeexSelectText(VirtualFile file) {
        /**
         * select the all file text
         */
        this.file = file;
        text = initText(null);
    }

    private String initText(String selectString) {
        if (!StringUtil.isEmpty(selectString)) {
            return selectString;
        }

        String text = null;
        try {
            text = getTextFromFile();
        } catch (IOException ignore) {
            ignore.printStackTrace();
            WeexShow.INSTANCE.showPopUp(ignore.toString());
        }

        return text;
    }


    public String getText() {
        return text;
    }


    public VirtualFile getVirturlFile() {
        return file;
    }

    private String getTextFromEdit(Editor editor) {

        if (editor == null)
            return null;

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectString = selectionModel.getSelectedText();
        return StringUtil.isEmpty(selectString) ? editor.getDocument().getText() : selectString;
    }


    private String getTextFromFile() throws IOException {
        StringBuilder s = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            s.append(line).append("\n");
        }
        return s.toString();
    }
}
