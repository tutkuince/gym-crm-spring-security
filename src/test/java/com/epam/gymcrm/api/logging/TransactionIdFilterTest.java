package com.epam.gymcrm.api.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TransactionIdFilterTest {

    @Test
    void testDoFilter_addsTransactionIdToMDC() throws Exception {
        TransactionIdFilter filter = new TransactionIdFilter();

        ServletRequest req = mock(ServletRequest.class);
        ServletResponse res = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        Mockito.doAnswer(invocation -> {
            String transactionId = MDC.get("transactionId");
            assertThat(transactionId)
                    .as("TransactionId should not be null")
                    .isNotNull();
            return null;
        }).when(chain).doFilter(req, res);

        filter.doFilter(req, res, chain);

        assertThat(MDC.get("transactionId")).isNull();
    }
}