package com.reliaquest.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {
        String msg = "Resource not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(msg);

        assertEquals(msg, ex.getMessage());
    }
}
