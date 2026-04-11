package com.example.web;

import com.example.web.model.User;
import com.example.web.testsupport.TestProgressExtension;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Gson 与实体字段对齐的回归测试（与 API JSON 契约相关）。
 */
@ExtendWith(TestProgressExtension.class)
class UserJsonRoundTripTest {

    private final Gson gson = new Gson();

    @Test
    void roundTrip_preservesCoreFields() {
        User u = new User();
        u.id = 42L;
        u.username = "alice";
        u.role = "TA";
        u.qmNumber = "QM123";
        u.setPasswordHash("$2a$10$hashed");

        String json = gson.toJson(u);
        User parsed = gson.fromJson(json, User.class);

        assertEquals(42L, parsed.id);
        assertEquals("alice", parsed.username);
        assertEquals("TA", parsed.role);
        assertEquals("QM123", parsed.qmNumber);
        assertEquals("$2a$10$hashed", parsed.getPasswordHash());
    }

    @Test
    void fromJson_omittedOptionalFieldsAreNull() {
        User parsed = gson.fromJson("{\"username\":\"bob\"}", User.class);
        assertEquals("bob", parsed.username);
        assertNull(parsed.id);
        assertNull(parsed.role);
    }
}
