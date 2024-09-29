package agileavengers.southwest_dashpass.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
    @Autowired
    private final UserDetailsService userDetailsService;

    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;


    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/landingpage.html").permitAll()
                        .requestMatchers("/signup.html").permitAll()
                        .requestMatchers("/login.html").permitAll()
                        .requestMatchers("/styles/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/employeeDashboard.html/**").hasAuthority("ROLE_EMPLOYEE")
                        .requestMatchers("/customerDashboard.html/**").hasAuthority("ROLE_CUSTOMER")
                        .anyRequest().authenticated() // Allow all requests temporarily for testing
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .successHandler(customLoginSuccessHandler)
                        .permitAll()
                )
                .logout( logout -> logout
                        .logoutUrl("/perform_logout") // logout url
                        .logoutSuccessUrl("/login.html?logout=true") //redirect after logout
                        .permitAll() //Allow everyone to access logout

                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password encoding
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);  // Inject your custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());  // Use BCrypt for password encoding
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.authenticationProvider(authenticationProvider());  // Use DaoAuthenticationProvider
        return auth.build();
    }

}


