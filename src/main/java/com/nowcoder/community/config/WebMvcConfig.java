package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.PrintWriter;

/**
 * @author yl
 * @date 2025-04-22 22:43
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Autowired
    private AlphaInterceptor alphainteceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketiterceptor;

    @Autowired
    private LogRequiredInterceptor logRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;


    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("开始注册自定义拦截器...");
        registry.addInterceptor(alphainteceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(loginTicketiterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg",
                        "/login", "/register", "/activation/**", "/kaptcha");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}
