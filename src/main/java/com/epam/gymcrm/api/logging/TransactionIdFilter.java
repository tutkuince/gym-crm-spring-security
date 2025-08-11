package com.epam.gymcrm.api.logging;

import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionIdFilter implements Filter {

    private static final String TRANSACTION_ID = "transactionId";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            String transactionId = UUID.randomUUID().toString();
            req.setAttribute(TRANSACTION_ID, transactionId);
            MDC.put(TRANSACTION_ID, transactionId);
            chain.doFilter(req, res);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}
