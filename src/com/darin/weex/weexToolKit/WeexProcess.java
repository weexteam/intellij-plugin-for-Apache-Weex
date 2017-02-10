package com.darin.weex.weexToolKit;

/**
 * Created by darin on 7/14/16.
 */
public class WeexProcess {
    public Process getProcess() {
        return process;
    }

    public long getPreviewServerPort() {
        return previewServerPort;
    }

    public long getWebServicePort() {
        return webServicePort;
    }

    private Process process;
    private long previewServerPort;
    private long webServicePort;

    public String getWeexFilePath() {
        return weexFilePath;
    }

    private String weexFilePath;

    private WeexProcess(Builder builder) {
        process = builder.getProcess();
        previewServerPort = builder.getPreviewServerPort();
        webServicePort = builder.getWebServicePort();
        weexFilePath = builder.getWeexFileName();
    }


    public boolean destory() {
        if (process == null)
            return true;

        process.destroy();
        try {
            process.exitValue();
        } catch (IllegalThreadStateException e) {
            return false;
        }

        process = null;
        weexFilePath = null;

        return true;
    }

    static class Builder {
        private Process process;

        public long getPreviewServerPort() {
            return previewServerPort;
        }

        public Process getProcess() {
            return process;
        }

        public long getWebServicePort() {
            return webServicePort;
        }

        private long previewServerPort = 8081;
        private long webServicePort = 8082;

        public String getWeexFileName() {
            return weexFileName;
        }

        private String weexFileName;

        public Builder(Process process) {
            this.process = process;
        }

        public Builder previewServerPort(long port) {
            previewServerPort = port;
            return this;
        }

        public Builder weexFileName(String name) {
            weexFileName = name;
            return this;
        }

        public Builder webServicePort(long port) {
            webServicePort = port;
            return this;
        }

        public WeexProcess build() {
            return new WeexProcess(this);
        }


    }
}
