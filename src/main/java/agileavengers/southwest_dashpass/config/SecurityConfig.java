package agileavengers.southwest_dashpass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permit access to these paths without authentication
                        .requestMatchers("/", "/register.html", "/login.html", "/css/**", "/js/**").permitAll()
                        // Secure these paths and require authentication
                        .requestMatchers("/dashboard/**").authenticated()
                )
                .csrf(csrf -> csrf.disable())  // Disable CSRF if you’re using non-HTML form submissions
                .formLogin(form -> form
                        .loginPage("/login.html")  // Custom login page
                        .loginProcessingUrl("/perform_login")  // Endpoint for processing login
                        .defaultSuccessUrl("/dashboard.html", true)  // Redirect to dashboard on successful login
                        .failureUrl("/login.html?error=true")  // Redirect to login on failure
                        .permitAll()  // Allow everyone to access the login page
                )
                .logout(logout -> logout
                        .logoutUrl("/perform_logout")  // Logout URL
                        .logoutSuccessUrl("/login.html?logout=true")  // Redirect after logout
                        .permitAll()  // Allow everyone to access logout
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password encoding
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager(); // AuthenticationManager for user authentication
    }
}

