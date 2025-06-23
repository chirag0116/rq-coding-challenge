package com.reliaquest.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ServiceUnavailableExceptionTest {

    @Test
    void testExceptionMessageAndCause() {
        String msg = "All retries failed";
        Throwable cause = new RuntimeException("Server unavailable");

        ServiceUnavailableException ex = new ServiceUnavailableException(msg, cause);

        assertEquals(msg, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
