package com.sinwoo.common.accesslog;

public interface AccessLogService {

    void write(AccessLogEntry entry);
}
