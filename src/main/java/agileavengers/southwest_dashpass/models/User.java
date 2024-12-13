package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name="users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Ensure this annotation is present
    @Column(name="id")
    private Long id;
    @Column(name="firstName", nullable = false)
    private String firstName;

    @Column(name="lastName", nullable = false)
    private String lastName;

    @Column(name="username", nullable = false, unique = true)
    private String username;

    @Column(name="email", nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="userType", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.CUSTOMER;  // Defaulting to CUSTOMER

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Customer customer;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Employee employee;

    // Constructor with parameters
    public User(String fname, String lname, String uname, String mail, String pword) {
        this.firstName = fname;
        this.lastName = lname;
        this.username = uname;
        this.email = mail;
        this.password = pword;
        this.userType = UserType.CUSTOMER;  // Set default or parameterize based on input
    }

    // Default constructor
    public User() {
        this.firstName = "";
        this.lastName = "";
        this.username = "";
        this.email = "";
        this.password = "";
        this.userType = UserType.CUSTOMER;  // Set a default value to avoid null
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userType.name()));  // Assuming userType is an enum or string representing role
        return authorities;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }



    // Getters and Setters
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", userType=" + userType +
                '}';
    }

    public Long getId() {
        return id;  // Getter for id
    }

    public void setId(Long id) {
        this.id = id;  // Setter for id
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;  // Correctly set the user type
    }

    public boolean isEnabled() {
        return true;  // Assuming all users are enabled by default; adjust if you have an actual enabled field
    }
}
