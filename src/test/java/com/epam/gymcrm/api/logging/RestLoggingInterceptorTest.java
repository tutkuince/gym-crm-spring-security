package com.epam.gymcrm.api.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestLoggingInterceptorTest {

    @Test
    void testPreHandle_and_AfterCompletion_logWithTransactionId() {
        RestLoggingInterceptor interceptor = new RestLoggingInterceptor();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        MDC.put("transactionId", "test-transaction-123");

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        assert MDC.get("transactionId").equals("test-transaction-123");

        MDC.remove("transactionId");
    }
}