package com.binuwara.AssetsFlow;

import com.binuwara.AssetsFlow.Entity.AuditLog;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class JsonbPersistenceTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void storesRawJsonAsJsonb() {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType("TEST");
        auditLog.setAction("TEST");
        auditLog.setNewValues("{\"role\":\"USER\"}");

        AuditLog saved = auditLogRepository.saveAndFlush(auditLog);
        String jsonType = jdbcTemplate.queryForObject(
                "select jsonb_typeof(new_values) from audit_logs where id = ?",
                String.class,
                saved.getId());

        assertEquals("object", jsonType);
    }
}
