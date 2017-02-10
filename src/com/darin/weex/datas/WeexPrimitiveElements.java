package com.darin.weex.datas;

import java.util.ArrayList;
import java.util.Arrays;

import static com.darin.weex.utils.WeexCmd.inputStreamToString;

/**
 * Created by darin on 5/17/16.
 */
public class WeexPrimitiveElements {
    private static ArrayList<String> primitiveEles = new ArrayList<String>();

    public static void initPrivitiveEles() {
        String[] eles = inputStreamToString(WeexPrimitiveElements.class.getResourceAsStream("/data/primitive_elements"), false).trim().replace("\r\n", "").replace("\n", "").replace("\r", "").split(",");
        primitiveEles.clear();
        primitiveEles.addAll(Arrays.asList(eles));
    }


    public static boolean isPrivitiveEle(String ele) {
        if (primitiveEles.size() == 0) {
            initPrivitiveEles();

        }
        return primitiveEles.contains(ele);
    }
}
