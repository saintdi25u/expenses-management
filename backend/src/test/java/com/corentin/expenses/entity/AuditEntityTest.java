package com.corentin.expenses.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEntityTest {

    @Test
    void onCreate_setsCreatedAtAndUpdatedAtToSameInstant() {
        AuditEntity entity = new AuditEntity();
        LocalDateTime before = LocalDateTime.now();

        entity.onCreate();

        assertThat(entity.getCreatedAt()).isAfterOrEqualTo(before).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(entity.getUpdatedAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void onUpdate_refreshesUpdatedAtOnly() {
        AuditEntity entity = new AuditEntity();
        LocalDateTime created = LocalDateTime.of(2020, 1, 1, 0, 0);
        entity.setCreatedAt(created);
        entity.setUpdatedAt(created);

        entity.onUpdate();

        assertThat(entity.getCreatedAt()).isEqualTo(created);
        assertThat(entity.getUpdatedAt()).isAfter(created);
    }
}
