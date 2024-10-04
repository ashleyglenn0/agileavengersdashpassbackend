package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;


@Entity
@Table(name="`user`")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Ensure this annotation is present
    @Column(name="ID")
    private Long id;
    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="username")
    private String username;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    @Column(name="user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.CUSTOMER;  // Defaulting to CUSTOMER

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

    // Getters and Setters
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
