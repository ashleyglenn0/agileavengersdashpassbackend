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

    @Autowired
    private PasswordEncoder passwordEncoder;


    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/landingpage").permitAll()
                        .requestMatchers("/signup").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/complete-registration").permitAll()
                        .requestMatchers("/styles/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/admin/cleanupPage").permitAll()
                        .requestMatchers("/admin/cleanupFlights").permitAll()
                        .requestMatchers("/admin/success").permitAll()
                        .requestMatchers("/employeedashboard/**").hasAuthority("ROLE_EMPLOYEE_SALES")
                        .requestMatchers("/employeedashboard/**").hasAuthority("ROLE_EMPLOYEE_MANAGER")
                        .requestMatchers("/employeedashboard/**").hasAuthority("ROLE_EMPLOYEE_SUPPORT")
                        .requestMatchers("/customerdashboard/**").hasAuthority("ROLE_CUSTOMER")
                        .anyRequest().authenticated() // Allow all requests temporarily for testing
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customLoginSuccessHandler)
                        .permitAll()
                )
                .logout( logout -> logout
                        .logoutUrl("/perform_logout") // logout url
                        .logoutSuccessUrl("/landingpage") //redirect after logout
                        .permitAll() //Allow everyone to access logout

                );

        return http.build();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);  // Inject your custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder);  // Use BCrypt for password encoding
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.authenticationProvider(authenticationProvider());  // Use DaoAuthenticationProvider
        return auth.build();
    }

}


