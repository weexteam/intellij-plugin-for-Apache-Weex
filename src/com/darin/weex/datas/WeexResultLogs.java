package com.darin.weex.datas;

/**
 * Created by darin on 5/17/16.
 */
public class WeexResultLogs {
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String line;
    private String column;
    private String reason;
    private String name;
}
