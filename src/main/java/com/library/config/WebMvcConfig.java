package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final OperationLogInterceptor operationLogInterceptor;
    private final String uploadDir;

    public WebMvcConfig(OperationLogInterceptor operationLogInterceptor,
                        @Value("${library.upload-dir:uploads}") String uploadDir) {
        this.operationLogInterceptor = operationLogInterceptor;
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            java.nio.file.Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create upload directory: " + uploadPath, e);
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.toUri().toString() + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/logs/**");
    }
}
