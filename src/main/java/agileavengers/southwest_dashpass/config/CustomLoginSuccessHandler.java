package agileavengers.southwest_dashpass.config;

import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;


@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService; // Inject UserService to access user details

    @Autowired
    public CustomLoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            return;
        }
        // Use the default redirect strategy provided by Spring Security
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // Determine the target URL based on user roles and include the customerId
    protected String determineTargetUrl(Authentication authentication) {
        // Get the principal, which is the Spring Security UserDetails object
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // Fetch the User entity from the UserService using the username
        User user = userService.findByUsername(principal.getUsername());

        // Get the authorities (roles) directly from the Authentication object
        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        // Check roles to determine the target URL
        boolean isCustomer = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CUSTOMER"));
        boolean isEmployee = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().startsWith("ROLE_EMPLOYEE"));

        // Redirect based on the user's role
        if (isCustomer) {
            Long customerId = user.getCustomer().getId();  // Retrieve the customer ID from the User object
            return "/customer/" + customerId + "/customerdashboard";  // Return the URL with customerId
        } else if (isEmployee) {
            Long employeeId = user.getEmployee().getId();
            return "/employee/" + employeeId + "/employeedashboard";  // Redirect to employee dashboard
        } else {
            throw new IllegalStateException("Unexpected user role: " + authorities);
        }
    }

}


