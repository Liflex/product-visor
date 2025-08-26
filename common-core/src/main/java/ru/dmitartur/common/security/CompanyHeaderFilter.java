package ru.dmitartur.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CompanyHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest req) {
                String companyId = req.getHeader("X-Company-Id");
                if (companyId != null && !companyId.isBlank()) {
                    CompanyContextHolder.setCompanyId(companyId);
                }
            }
            chain.doFilter(request, response);
        } finally {
            CompanyContextHolder.clear();
        }
    }
}


