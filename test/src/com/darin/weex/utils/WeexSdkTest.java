package com.darin.weex.utils;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by dyy on 2017/2/19.
 */
class WeexSdkTest {
    @Test
    void transform() throws Exception {

        String cmd = "\"C:\\Program Files\\nodejs\\node.exe\" \"D:\\Project\\dyy\\weex-intellij-plugin\\.sandbox\\config\\weex-tool\\weex-transformer\\bin\\transformer.js\" \"D:/Project/temp/tempTest/src/Htkki.we\" -o \"D:\\Project\\dyy\\weex-intellij-plugin\\.sandbox\\config\\weex-tool\\weex-html5\"";
        Process process = Runtime.getRuntime().exec(cmd);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder s = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            s.append(line);
            s.append("\n");
        }

        System.out.println(s.toString());
    }

}