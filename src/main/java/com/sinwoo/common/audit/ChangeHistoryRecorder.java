package com.sinwoo.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangeHistoryRecorder {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void record(BaseEntity entity, ChangeType changeType) {
        jdbcTemplate.update(
                """
                INSERT INTO TB_CHG_HIST (
                    TBL_NM,
                    ENT_NM,
                    ROW_ID,
                    CHG_TP,
                    TENANT_ID,
                    SNAP_JSON,
                    CHG_BY,
                    CHG_DTM,
                    CRT_BY,
                    CRT_DTM,
                    UPD_BY,
                    UPD_DTM
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                resolveTableName(entity),
                entity.getClass().getSimpleName(),
                entity.getId(),
                changeType.name(),
                resolveTenantId(entity),
                serialize(entity),
                resolveChangedBy(entity),
                OffsetDateTime.now(),
                resolveChangedBy(entity),
                OffsetDateTime.now(),
                resolveChangedBy(entity),
                OffsetDateTime.now()
        );
    }

    private String resolveTableName(BaseEntity entity) {
        Table table = entity.getClass().getAnnotation(Table.class);
        return table != null ? table.name() : entity.getClass().getSimpleName().toUpperCase();
    }

    private Long resolveTenantId(BaseEntity entity) {
        Object tenantId = readField(entity, "tenantId");
        if (tenantId instanceof Long value) {
            return value;
        }
        return "TB_TENANT".equals(resolveTableName(entity)) ? entity.getId() : null;
    }

    private String resolveChangedBy(BaseEntity entity) {
        return entity.getUpdatedBy() != null ? entity.getUpdatedBy() : entity.getCreatedBy();
    }

    private String serialize(BaseEntity entity) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        Class<?> current = entity.getClass();

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    snapshot.putIfAbsent(field.getName(), field.get(entity));
                } catch (IllegalAccessException ignored) {
                    snapshot.putIfAbsent(field.getName(), null);
                }
            }
            current = current.getSuperclass();
        }

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize change history payload.", exception);
        }
    }

    private Object readField(BaseEntity entity, String fieldName) {
        Class<?> current = entity.getClass();
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(entity);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (IllegalAccessException exception) {
                return null;
            }
        }
        return null;
    }
}
