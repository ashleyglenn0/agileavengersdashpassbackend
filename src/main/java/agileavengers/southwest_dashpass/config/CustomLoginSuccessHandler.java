package agileavengers.southwest_dashpass.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;



@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            return;
        }

        // Use the default redirect strategy provided by Spring Security
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // Determine the target URL based on user roles
    protected String determineTargetUrl(Authentication authentication) {
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("EMPLOYEE"));
        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("CUSTOMER"));

        if (isEmployee) {
            return "/employeedashboard";
        } else if (isCustomer) {
            return "/customerdashboard";
        } else {
            throw new IllegalStateException("Unexpected user role");
        }
    }
}


