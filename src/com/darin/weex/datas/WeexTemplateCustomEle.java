package com.darin.weex.datas;

import static com.darin.weex.utils.WeexCmd.inputStreamToString;

/**
 * Created by darin on 5/17/16.
 */
public class WeexTemplateCustomEle {
    public static String templateString;

    public static void initTemplateString() {
        templateString = inputStreamToString(WeexTemplateCustomEle.class.getResourceAsStream("/data/element"), true);
    }
}
