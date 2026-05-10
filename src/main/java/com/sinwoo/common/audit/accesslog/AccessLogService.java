package com.sinwoo.common.audit.accesslog;

public interface AccessLogService {

    void write(AccessLogEntry entry);
}
