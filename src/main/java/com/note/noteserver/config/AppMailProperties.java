package com.note.noteserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {
    private String from = "no-reply@example.com";
    private String subjectPrefix = "[Note] ";
}
