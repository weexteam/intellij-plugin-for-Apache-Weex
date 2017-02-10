package com.darin.weex.datas;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.darin.weex.utils.WeexCmd.inputStreamToString;

/**
 * Created by darin on 5/17/16.
 */
public class WeexElementsParser {
    private String code;
    private String pattern = "(?<=</).*?(?<=>)";
    private VirtualFile file;
    private HashMap<String, String> elementsMap = new HashMap<String, String>();

    public static String ERROR_KEY = "weex_Plugin_Error_this_should_not_be_overrided";

    public WeexElementsParser(WeexSelectText weexSelectText) {
        file = weexSelectText.getVirturlFile();
        code = weexSelectText.getText();

        ArrayList<String> currentPathFiles = getCurrentPathFiles();

        Matcher m = Pattern.compile(pattern, Pattern.CANON_EQ).matcher(code);
        ArrayList<String> elements = new ArrayList<String>();
        File currentPath = new File(file.getParent().getPath());

        while (m.find())
            elements.add(m.group().split(">")[0]);


        StringBuilder errorString = new StringBuilder();

        for (String ele : elements) {
            String eleFileName = ele + ".we";
            if (currentPathFiles.contains(eleFileName))
                elementsMap.put(ele, currentPath + File.separator + eleFileName);
            else if (!WeexPrimitiveElements.isPrivitiveEle(ele))
                errorString.append("The custom element " + ele + " is not found in " + currentPath).append("\r\n");
        }

        String error = errorString.toString();
        if (!StringUtil.isEmpty(error))
            elementsMap.put(ERROR_KEY, error);
        System.out.print("");
    }

    public HashMap<String, String> getCustormElements() {
        return elementsMap;
    }


    /**
     * @param transformToLowerCase get the real code in lower case
     * @return the real code
     */
    public String getRealCode(boolean transformToLowerCase) {

        StringBuilder realCode = new StringBuilder();

        Iterator<Map.Entry<String, String>> entryIterator = elementsMap.entrySet().iterator();

        Map.Entry<String, String> ele;
        while (entryIterator.hasNext()) {
            ele = entryIterator.next();
            String realEle = getRealEle(ele.getKey(), ele.getValue());
            if (StringUtil.isEmpty(realEle))
                continue;
            realCode.append(realEle);
        }

        realCode.append(code);

        String realCodeString = realCode.toString();

        if (transformToLowerCase)
            for (String element : elementsMap.keySet()) {
                //upper case is unavailable for playground
                realCodeString = realCodeString.replace(element, element.toLowerCase());
            }

        return realCodeString;
    }


    private ArrayList<String> getCurrentPathFiles() {
        ArrayList<String> currentDirFiles = new ArrayList<String>();
        if (file == null)
            return currentDirFiles;

        File currentPath = new File(file.getParent().getPath());
        String[] files = currentPath.list();

        if (files != null)
            Collections.addAll(currentDirFiles, files);

        return currentDirFiles;
    }

    private String getRealEle(String name, String path) {
        String realeEle = null;
        File weexScriptFile = new File(path);
        if (weexScriptFile.isDirectory())
            return null;
        String temp = null;
        try {
            temp = inputStreamToString(new FileInputStream(weexScriptFile), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!StringUtil.isEmpty(temp)) {
            realeEle = WeexTemplateCustomEle.templateString.replace("elename", name).replace("code", temp);
        }
        return realeEle;
    }


}
