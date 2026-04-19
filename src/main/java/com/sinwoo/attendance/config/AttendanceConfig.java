package com.sinwoo.attendance.config;

import com.sinwoo.attendance.support.AttendanceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AttendanceProperties.class)
public class AttendanceConfig {
}
