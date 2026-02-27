package com.klzw.common.database.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import com.klzw.common.database.properties.DatabaseProperties;
import com.klzw.common.database.util.DruidConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Druid 连接池配置类
 * 用于配置 Druid 连接池的监控和其他高级特性
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
public class DruidConfig {

    private final DatabaseProperties databaseProperties;

    public DruidConfig(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    /**
     * 配置 Druid 监控 Servlet
     * @return Servlet 注册bean
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ServletRegistrationBean druidStatViewServlet() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        registrationBean.setServlet(new StatViewServlet());
        registrationBean.addUrlMappings("/druid/*");
        
        DatabaseProperties.Druid druidConfig = databaseProperties.getDruid();
        
        registrationBean.addInitParameter("loginUsername", druidConfig.getStatViewUsername());
        registrationBean.addInitParameter("loginPassword", druidConfig.getStatViewPassword());
        registrationBean.addInitParameter("allow", druidConfig.getStatViewAllow());
        
        return registrationBean;
    }

    /**
     * 配置 Druid 监控 Filter
     * @return Filter 注册bean
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public FilterRegistrationBean druidWebStatFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new WebStatFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        
        return registrationBean;
    }

    /**
     * 配置 Druid 连接池参数
     * @param dataSource 数据源
     * @return 配置后的数据源
     * @deprecated 使用 DruidConfigUtils.configureDruidDataSource 替代
     */
    @Deprecated
    public DataSource configureDruidDataSource(DruidDataSource dataSource) {
        DruidConfigUtils.configureDruidDataSource(dataSource, databaseProperties);
        return dataSource;
    }
}