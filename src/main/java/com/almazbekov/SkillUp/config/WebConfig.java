package com.almazbekov.SkillUp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.FormattingConversionService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
                
        registry.addResourceHandler("/courses/images/**")
                .addResourceLocations("file:" + uploadDir + "/courses/images/");
                
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
                
        registry.addResourceHandler("/assignments/files/**")
                .addResourceLocations("file:uploads/assignments/files/");
                
        registry.addResourceHandler("/submissions/files/**")
                .addResourceLocations("file:uploads/submissions/files/");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);

        // Добавляем конвертер для строки в LocalDateTime
        registry.addConverter(new StringToLocalDateTimeConverter());
    }

    private static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            try {
                return LocalDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // Если не удалось распарсить как ISO_DATE_TIME, пробуем другие форматы
                try {
                    return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e2) {
                    throw new IllegalArgumentException("Не удалось преобразовать строку в LocalDateTime: " + source, e2);
                }
            }
        }
    }
} 