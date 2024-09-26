package agileavengers.southwest_dashpass.config;

import agileavengers.southwest_dashpass.models.UserType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    private UserType userType;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Set<String> userType = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Check user's role and redirect accordingly
        if (userType.contains("EMPLOYEE")) {
            response.sendRedirect("/employeeDashboard");  // Employee dashboard
        } else if (userType.contains("CUSTOMER")) {
            response.sendRedirect("/customerDashboard");  // Customer dashboard
        } else {
            response.sendRedirect("/error");  // Default dashboard
        }
    }

}

