package com.klzw.common.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.file.storage")
public class FileStorageProperties {
    private boolean enabled = true;
    private long maxFileSize = 10 * 1024 * 1024L;
    private String allowedExtensions = "jpg,jpeg,png,gif,bmp,pdf,doc,docx,xls,xlsx";
    private String defaultModule = "user";
    
    private DualWrite dualWrite = new DualWrite();

    @Data
    public static class DualWrite {
        private boolean enabled = false;
        private String primary = "minio";
    }
}
