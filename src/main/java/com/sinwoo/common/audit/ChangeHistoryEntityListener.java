package com.sinwoo.common.audit;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;

public class ChangeHistoryEntityListener {

    @PostPersist
    public void postPersist(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            SpringContextHolder.getBean(ChangeHistoryRecorder.class).record(baseEntity, ChangeType.INSERT);
        }
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            SpringContextHolder.getBean(ChangeHistoryRecorder.class).record(baseEntity, ChangeType.UPDATE);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            SpringContextHolder.getBean(ChangeHistoryRecorder.class).record(baseEntity, ChangeType.DELETE);
        }
    }
}
