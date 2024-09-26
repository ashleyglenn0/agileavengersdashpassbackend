package agileavengers.southwest_dashpass.config;

import agileavengers.southwest_dashpass.security.CustomUserDetailsService;
import agileavengers.southwest_dashpass.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;
    @Autowired
    private UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permit access to these paths without authentication
                        .requestMatchers("/", "/signup","/login","/css/**", "/js/**").permitAll()
                        // Secure these paths and require authentication
                        .requestMatchers("/" + customLoginSuccessHandler.toString()).authenticated()
                )
                .csrf(csrf -> csrf.disable())  // Disable CSRF if you’re using non-HTML form submissions
                .formLogin(form -> form
                        .loginPage("/login")  // Custom login page
                        .defaultSuccessUrl("/" + customLoginSuccessHandler.toString(), true)  // Redirect to dashboard on successful login
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
public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, CustomUserDetailsService userDetailsService) throws Exception {
    return http
            .getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder)
            .and()
            .build();
}
}

