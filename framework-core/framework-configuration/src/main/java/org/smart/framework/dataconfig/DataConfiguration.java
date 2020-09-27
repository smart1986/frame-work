package org.smart.framework.dataconfig;

import java.io.File;

public class DataConfiguration {
    /**
     * 配置文件路径
     */
    private String path = "dataconfig" + File.separator;

    /**
     * 数据配置映射对应的包
     */
    private String packageScan=".";

    /**
     * 配置文件扩展名
     */
    private String extension = ".json";

    private long flushTime = 10000L;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPackageScan() {
        return packageScan;
    }

    public void setPackageScan(String packageScan) {
        this.packageScan = packageScan;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public long getFlushTime() {
        return flushTime;
    }

    public void setFlushTime(long flushTime) {
        this.flushTime = flushTime;
    }
}
