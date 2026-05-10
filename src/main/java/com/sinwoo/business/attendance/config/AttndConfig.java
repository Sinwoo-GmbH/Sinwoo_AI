package com.sinwoo.business.attendance.config;

import com.sinwoo.business.attendance.support.AttndProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AttndProperties.class)
public class AttndConfig {
}
