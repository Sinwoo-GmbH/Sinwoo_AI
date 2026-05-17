package com.sinwoo;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SinwooBackendApplication {

    public static void main(String[] args) {
        // JVM 타임존을 UTC로 고정 — hibernate.jdbc.time_zone=UTC 와 일치시킴
        // (Windows 시스템 타임존이 Asia/Seoul 등일 때 TIME 컬럼 변환 방지)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(SinwooBackendApplication.class, args);
    }
}
