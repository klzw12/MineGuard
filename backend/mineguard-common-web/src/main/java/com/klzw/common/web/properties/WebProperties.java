package com.klzw.common.web.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.web")
public class WebProperties {

    /**
     * 是否启用Web配置
     */
    private boolean enabled = true;

    /**
     * 资源配置
     */
    private Resources resources = new Resources();

    /**
     * 文件上传配置
     */
    private FileUpload fileUpload = new FileUpload();

    @Data
    public static class Resources {
        /**
         * 静态资源位置
         */
        private String staticLocations = "classpath:/static/";

        /**
         * 缓存周期
         */
        private int cachePeriod = 3600;
    }

    @Data
    public static class FileUpload {
        /**
         * 上传位置
         */
        private String location = "${java.io.tmpdir}";

        /**
         * 最大文件大小
         */
        private String maxFileSize = "10MB";

        /**
         * 最大请求大小
         */
        private String maxRequestSize = "100MB";
    }
}