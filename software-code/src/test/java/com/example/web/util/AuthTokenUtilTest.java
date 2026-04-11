package com.example.web.util;

import com.example.web.testsupport.TestProgressExtension;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestProgressExtension.class)
class AuthTokenUtilTest {

    @RepeatedTest(5)
    void generateAuthToken_returnsNonEmptyAndUnique() {
        String a = AuthTokenUtil.generateAuthToken();
        String b = AuthTokenUtil.generateAuthToken();
        assertNotNull(a);
        assertNotNull(b);
        assertFalse(a.isBlank());
        assertFalse(b.isBlank());
        assertNotEquals(a, b);
    }

    @Test
    void generateSessionKey_sameShapeAsAuthToken() {
        String key = AuthTokenUtil.generateSessionKey();
        assertTrue(AuthTokenUtil.isValidToken(key));
    }

    @Test
    void isValidToken_rejectsNullBlankAndShort() {
        assertFalse(AuthTokenUtil.isValidToken(null));
        assertFalse(AuthTokenUtil.isValidToken(""));
        assertFalse(AuthTokenUtil.isValidToken("   "));
        assertFalse(AuthTokenUtil.isValidToken("short"));
    }

    @Test
    void isValidToken_acceptsTypicalToken() {
        assertTrue(AuthTokenUtil.isValidToken(AuthTokenUtil.generateAuthToken()));
    }
}
